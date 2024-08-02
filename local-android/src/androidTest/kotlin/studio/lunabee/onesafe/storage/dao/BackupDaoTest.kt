/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 6/21/2023 - for the oneSafe6 SDK.
 * Last modified 6/21/23, 5:07 PM
 */

package studio.lunabee.onesafe.storage.dao

import androidx.core.database.getStringOrNull
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.extension.testInsertCloud
import studio.lunabee.onesafe.storage.extension.testInsertLocal
import studio.lunabee.onesafe.storage.model.RoomBackup
import studio.lunabee.onesafe.storage.utils.toSqlBlobString
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals

// TODO <multisafe> safeId testing

@HiltAndroidTest
class BackupDaoTest {

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var dao: BackupDao

    @Inject lateinit var clock: Clock

    @Inject lateinit var mainDatabase: MainDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun refreshCloudBackups_test(): TestResult = runTest {
        dao.testInsertCloud("to_be_deleted")
        dao.testInsertCloud("to_be_deleted_2")

        val localBackups = listOf(
            dao.testInsertLocal("not_to_be_deleted"),
            dao.testInsertLocal("not_to_be_deleted_2"),
        )

        val remoteBackups = listOf(
            CloudBackup(testUUIDs[1].toString(), testUUIDs[1].toString(), Instant.now(clock).plusMillis(1), firstSafeId),
            CloudBackup(testUUIDs[0].toString(), testUUIDs[0].toString(), Instant.now(clock), firstSafeId),
        )

        dao.refreshCloudBackups(remoteBackups)
        val actualLocals = dao.getAllLocal(firstSafeId)
        val actualClouds = dao.getAllCloud(firstSafeId)

        // assert keep local backups
        assertContentEquals(localBackups, actualLocals)
        // assert cloud backups inserted & cleaned
        assertContentEquals(remoteBackups, actualClouds.map { CloudBackup(it.remoteId, it.name, it.date, firstSafeId) })
    }

    @Test
    fun deleteCloudBackup_test(): TestResult = runTest {
        dao.testInsertCloud("backup_1")
        dao.testInsertCloud("backup_2")

        val localBackups = listOf(
            dao.testInsertLocal("backup_2"),
        )

        dao.deleteCloudBackup("backup_1")
        // Assert backup_1 deleted & backup_2 kept
        assertContentEquals(
            localBackups.map { RoomBackup(it.id, "backup_2", it.localFile, it.date, it.safeId, "backup_2") },
            getAll(firstSafeId),
        )

        // Assert backup_2 kept with remoteId nullified
        dao.deleteCloudBackup("backup_2")
        assertContentEquals(
            localBackups.map { RoomBackup(it.id, null, it.localFile, it.date, it.safeId, "backup_2") },
            getAll(firstSafeId),
        )
    }

    @Test
    fun deleteLocalBackup_test(): TestResult = runTest {
        dao.testInsertLocal("backup_1")
        dao.testInsertLocal("backup_2")

        val cloudBackups = listOf(
            dao.testInsertCloud("backup_2"),
        )

        dao.deleteLocalBackup("backup_1")
        // Assert backup_1 deleted & backup_2 kept
        assertContentEquals(
            cloudBackups.map { RoomBackup(it.id, it.remoteId, File("backup_2"), it.date, firstSafeId, "backup_2") },
            getAll(firstSafeId),
        )

        // Assert backup_2 kept with localFile nullified
        dao.deleteLocalBackup("backup_2")
        assertContentEquals(
            cloudBackups.map { RoomBackup(it.id, it.remoteId, null, it.date, firstSafeId, "backup_2") },
            getAll(firstSafeId),
        )
    }

    private fun getAll(safeId: SafeId): List<RoomBackup> {
        val safeIdBlob = safeId.toByteArray().toSqlBlobString()
        val cursor = mainDatabase.openHelper.readableDatabase.query(
            "SELECT * FROM Backup WHERE safe_id IS $safeIdBlob ORDER BY `date` DESC",
        )
        return generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {
                RoomBackup(
                    id = cursor.getString(0),
                    remoteId = cursor.getString(1),
                    localFile = cursor.getStringOrNull(2)?.let { File(it) },
                    date = Instant.ofEpochMilli(cursor.getLong(3)),
                    safeId = SafeId(cursor.getBlob(4)),
                    name = cursor.getString(5),
                )
            }.toList()
    }
}
