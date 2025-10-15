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
 * Created by Lunabee Studio / Date - 10/4/2023 - for the oneSafe6 SDK.
 * Last modified 10/4/23, 4:30 PM
 */

package studio.lunabee.onesafe.storage.datasource

import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.dao.SafeDao
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class BackupLocalDataSourceImplTest {
    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var backupLocalDataSource: LocalBackupLocalDataSourceImpl

    @Inject internal lateinit var backupDao: BackupDao

    @Inject internal lateinit var mainDatabase: MainDatabase

    @Inject lateinit var clock: Clock

    @Inject internal lateinit var safeDao: SafeDao

    @Inject
    @InternalDir(InternalDir.Type.Backups)
    lateinit var backupDir: File

    private val tempDir = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "temp")

    @Before
    fun setUp() {
        hiltRule.inject()
        backupDir.deleteRecursively()
        tempDir.deleteRecursively()
        tempDir.mkdirs()
        runTest {
            safeDao.insert(CommonTestUtils.roomSafe())
        }
    }

    @After
    fun tearsDown() {
        backupDir.deleteRecursively()
        tempDir.deleteRecursively()
    }

    /**
     * Main path (add backup -> get backup -> ok)
     */
    @Test
    fun addBackup_getBackups_test(): TestResult = runTest {
        val backupFile = File(tempDir, testUUIDs[0].toString())
        backupFile.createNewFile()
        val localBackup = LocalBackup(Instant.now(clock), backupFile, SafeId(testUUIDs[0]))
        val expected = listOf(
            LBResult.Success(
                localBackup.copy(file = File(backupDir, backupFile.name)),
            ),
        )

        backupLocalDataSource.addBackup(localBackup)
        val actual = backupLocalDataSource.getBackups(firstSafeId)
        assertContentEquals(expected, actual)
    }

    /**
     * Add backup with non existent file -> Expect error + no entry in db
     */
    @Test
    fun addBackup_error_test(): TestResult = runTest {
        val error = assertThrows<OSStorageError> {
            val backupFile = File(tempDir, testUUIDs[0].toString())
            val localBackup = LocalBackup(Instant.now(clock), backupFile, SafeId(testUUIDs[0]))
            backupLocalDataSource.addBackup(localBackup)
        }
        assertEquals(OSStorageError.Code.UNKNOWN_FILE_ERROR, error.code)

        val count = withContext(Dispatchers.IO) {
            mainDatabase.query("SELECT * FROM Backup", null).count
        }
        assertEquals(0, count)
    }

    /**
     * Add backup -> delete file manually -> get backups -> Expect failure result
     */
    @Test
    fun getBackups_error_test(): TestResult = runTest {
        val safeId = SafeId(testUUIDs[0])
        val filename = testUUIDs[0].toString()
        val backupFile = File(tempDir, filename)
        backupFile.createNewFile()
        val localBackup = LocalBackup(Instant.now(clock), backupFile, safeId)
        val expected = listOf(
            LBResult.Failure(
                throwable = OSStorageError.Code.MISSING_BACKUP_FILE.get(),
                failureData = localBackup.copy(file = File(backupDir, backupFile.name)),
            ),
        )

        backupLocalDataSource.addBackup(localBackup)
        backupDao.getLocalById(localBackup.id)!!.localFile.delete()
        val actual = backupLocalDataSource.getBackups(firstSafeId)
        assertContentEquals(expected, actual)
    }

    /**
     * Main path (add -> delete -> get -> db empty & file deleted)
     */
    @Test
    fun delete_test(): TestResult = runTest {
        val backupFile = File(tempDir, testUUIDs[0].toString())
        backupFile.createNewFile()
        val localBackup = backupLocalDataSource
            .addBackup(LocalBackup(Instant.now(clock), backupFile, SafeId(testUUIDs[0])))

        assertTrue(backupLocalDataSource.getBackups(firstSafeId).isNotEmpty())
        assertTrue(localBackup.file.exists())
        backupLocalDataSource.delete(listOf(localBackup))
        assertTrue(backupLocalDataSource.getBackups(firstSafeId).isEmpty())
        assertFalse(localBackup.file.exists())
    }
}
