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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 4:48 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals

class DeleteOldLocalBackupsUseCaseTest {
    private val keepBackupsNumber: Int = 5
    private val backupDb = mutableMapOf<String, LocalBackup>()
    private val backupRepository: LocalBackupRepository = mockk {
        coEvery { addBackup(any()) } answers {
            val backup = firstArg<LocalBackup>()
            backupDb[backup.file.name] = backup
        }
        coEvery { delete(any(), firstSafeId) } answers {
            firstArg<List<LocalBackup>>().forEach {
                backupDb.remove(it.file.name)
            }
        }
        coEvery { getBackups(firstSafeId) } answers { backupDb.values.map { LBResult.Success(it) } }
    }
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository = mockk {
        coEvery { autoBackupMaxNumber(firstSafeId) } returns keepBackupsNumber
    }
    private val useCase: DeleteOldLocalBackupsUseCase = DeleteOldLocalBackupsUseCase(
        backupRepository = backupRepository,
        autoBackupSettingsRepository = autoBackupSettingsRepository,
    )

    @Before
    fun setUpEach() {
        backupDb.clear()
    }

    companion object {
        private const val ext: String = ".${ImportExportConstant.ExtensionOs6Backup}"
        private val tmpDir = File("DeleteOldLocalBackupsUseCaseTest")

        @BeforeClass
        @JvmStatic
        fun setUp() {
            tmpDir.mkdir()
        }

        @AfterClass
        @JvmStatic
        fun tearsDown() {
            tmpDir.deleteRecursively()
        }
    }

    @Test
    fun empty_test(): TestResult = runTest {
        useCase(firstSafeId)
        val actual = backupRepository.getBackups(firstSafeId)
        assertContentEquals(emptyList(), actual)
    }

    @Test
    fun nothing_to_delete_test(): TestResult = runTest {
        repeat(5) {
            val file = File(tmpDir, "test-00000101-00000$it$ext")
            backupDb[file.name] = LocalBackup(date = Instant.ofEpochSecond(it.toLong()), file = file, safeId = SafeId(testUUIDs[0]))
            file.createNewFile()
        }

        val expected = backupRepository.getBackups(firstSafeId)
        useCase(firstSafeId)
        val actual = backupRepository.getBackups(firstSafeId)
        assertContentEquals(expected, actual)
    }

    @Test
    fun delete_oldest_test(): TestResult = runTest {
        val localBackups = listOf(
            LocalBackup(date = Instant.ofEpochSecond(5), file = File(tmpDir, "test-00000101-000005$ext"), safeId = SafeId(testUUIDs[0])),
            LocalBackup(date = Instant.ofEpochSecond(4), file = File(tmpDir, "test-00000101-000004$ext"), safeId = SafeId(testUUIDs[0])),
            LocalBackup(date = Instant.ofEpochSecond(3), file = File(tmpDir, "test-00000101-000003$ext"), safeId = SafeId(testUUIDs[0])),
            LocalBackup(date = Instant.ofEpochSecond(2), file = File(tmpDir, "test-00000101-000002$ext"), safeId = SafeId(testUUIDs[0])),
            LocalBackup(date = Instant.ofEpochSecond(1), file = File(tmpDir, "test-00000101-000001$ext"), safeId = SafeId(testUUIDs[0])),
            LocalBackup(date = Instant.ofEpochSecond(0), file = File(tmpDir, "test-00000101-000000$ext"), safeId = SafeId(testUUIDs[0])),
        )
        localBackups.forEach {
            backupDb[it.file.name] = it
            it.file.createNewFile()
        }

        val expected = localBackups.take(keepBackupsNumber)
        useCase(firstSafeId)
        val actual = backupRepository.getBackups(firstSafeId).map { it.data!! }
        assertContentEquals(expected, actual)
    }
}
