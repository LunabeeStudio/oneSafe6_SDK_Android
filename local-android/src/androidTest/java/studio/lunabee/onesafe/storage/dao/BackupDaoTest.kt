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

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.storage.extension.testInsertCloud
import studio.lunabee.onesafe.storage.extension.testInsertLocal
import studio.lunabee.onesafe.storage.model.RoomBackup
import studio.lunabee.onesafe.test.testClock
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class BackupDaoTest {

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var dao: BackupDao

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
            CloudBackup(testUUIDs[1].toString(), testUUIDs[1].toString(), Instant.now(testClock).plusMillis(1)),
            CloudBackup(testUUIDs[0].toString(), testUUIDs[0].toString(), Instant.now(testClock)),
        )

        dao.refreshCloudBackups(remoteBackups)
        val actualLocals = dao.getAllLocal()
        val actualClouds = dao.getAllCloud()

        // assert keep local backups
        assertContentEquals(localBackups, actualLocals)
        // assert cloud backups inserted & cleaned
        assertContentEquals(remoteBackups, actualClouds.map { CloudBackup(it.remoteId, it.id, it.date) })
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
        assertContentEquals(localBackups.map { RoomBackup(it.id, "backup_2", it.localFile, it.date) }, dao.getAll())

        // Assert backup_2 kept with remoteId nullified
        dao.deleteCloudBackup("backup_2")
        assertContentEquals(localBackups.map { RoomBackup(it.id, null, it.localFile, it.date) }, dao.getAll())
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
        assertContentEquals(cloudBackups.map { RoomBackup(it.id, it.remoteId, File("backup_2"), it.date) }, dao.getAll())

        // Assert backup_2 kept with localFile nullified
        dao.deleteLocalBackup("backup_2")
        assertContentEquals(cloudBackups.map { RoomBackup(it.id, it.remoteId, null, it.date) }, dao.getAll())
    }
}
