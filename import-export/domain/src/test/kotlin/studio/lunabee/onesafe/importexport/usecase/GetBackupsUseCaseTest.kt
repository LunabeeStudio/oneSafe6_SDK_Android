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
 * Last modified 10/2/23, 1:10 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.repository.BackupRepository
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.test.OSTestUtils
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertContentEquals

class GetBackupsUseCaseTest {
    private val backupRepository: BackupRepository = mockk()
    private val useCase: GetBackupsUseCase = GetBackupsUseCase(
        backupRepository = backupRepository,
    )

    companion object {
        private const val ext: String = ".${ImportExportConstant.ExtensionOs6Backup}"
        private val tmpDir = File("GetBackupsUseCaseTest")

        @BeforeAll
        @JvmStatic
        fun setUp() {
            tmpDir.mkdir()
        }

        @AfterAll
        @JvmStatic
        fun tearsDown() {
            tmpDir.deleteRecursively()
        }
    }

    // test parsing valid & ordering
    @Test
    fun parse_and_order_test() {
        val files = listOf(
            File(tmpDir, "test-00010101-000000$ext"),
            File(tmpDir, "test-00000101-000001$ext"),
            File(tmpDir, "test-00000101-000000$ext"),
        )
        files.forEach { it.createNewFile() }

        val expected = listOf(
            Backup(LocalDateTime.of(1, 1, 1, 0, 0, 0), files[0]),
            Backup(LocalDateTime.of(0, 1, 1, 0, 0, 1), files[1]),
            Backup(LocalDateTime.of(0, 1, 1, 0, 0, 0), files[2]),
        )

        repeat(5) {
            every { backupRepository.getBackups() } returns files.shuffled(OSTestUtils.random)
            val actual = useCase()
            assertContentEquals(expected, actual)
        }
    }

    // test wrong extension file ignored
    @Test
    fun ignore_bad_ext_test() {
        val badExtFile = File(tmpDir, "test-00010101-000000.badExt").also {
            it.createNewFile()
        }
        every { backupRepository.getBackups() } returns listOf(badExtFile)
        val actual = useCase()
        assertContentEquals(emptyList(), actual)
    }

    // test wrong file name pattern
    @Test
    fun ignore_split_error_test() {
        val badSplit = File(tmpDir, "split_error$ext").also {
            it.createNewFile()
        }
        every { backupRepository.getBackups() } returns listOf(badSplit)
        val actual = useCase()
        assertContentEquals(emptyList(), actual)
    }

    // test wrong date/time pattern
    @Test
    fun ignore_date_pattern_error_test() {
        val badDate = File(tmpDir, "test-abc-123$ext").also {
            it.createNewFile()
        }
        every { backupRepository.getBackups() } returns listOf(badDate)
        val actual = useCase()
        assertContentEquals(emptyList(), actual)
    }
}
