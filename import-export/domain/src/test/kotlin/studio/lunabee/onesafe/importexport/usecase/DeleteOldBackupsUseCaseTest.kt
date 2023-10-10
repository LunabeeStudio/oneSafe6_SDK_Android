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

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.repository.BackupRepository
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import kotlin.test.assertContentEquals

class DeleteOldBackupsUseCaseTest {
    private val backupRepository: BackupRepository = mockk()
    private val getBackupsUseCase: GetBackupsUseCase = GetBackupsUseCase(backupRepository)
    private val useCase: DeleteOldBackupsUseCase = DeleteOldBackupsUseCase(
        getBackupsUseCase = getBackupsUseCase,
    )

    companion object {
        private const val ext: String = ".${ImportExportConstant.ExtensionOs6Backup}"
        private val tmpDir = File("DeleteOldBackupsUseCaseTest")

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

    @Test
    fun empty_test() {
        val files = emptyList<File>()
        every { backupRepository.getBackups() } returns files

        val expected = getBackupsUseCase.invoke()
        useCase()
        val actual = getBackupsUseCase.invoke()
        assertContentEquals(expected, actual)
    }

    @Test
    fun nothing_to_delete_test() {
        val files = List(5) {
            File(tmpDir, "test-00000101-00000$it$ext")
        }
        files.forEach { it.createNewFile() }
        every { backupRepository.getBackups() } returns files

        val expected = getBackupsUseCase.invoke()
        useCase()
        val actual = getBackupsUseCase.invoke()
        assertContentEquals(expected, actual)
    }

    @Test
    fun delete_oldest_test() {
        val files = listOf(
            File(tmpDir, "test-00000101-000005$ext"),
            File(tmpDir, "test-00000101-000004$ext"),
            File(tmpDir, "test-00000101-000003$ext"),
            File(tmpDir, "test-00000101-000002$ext"),
            File(tmpDir, "test-00000101-000001$ext"),
            File(tmpDir, "test-00000101-000000$ext"),
        )
        files.forEach { it.createNewFile() }
        every { backupRepository.getBackups() } returns files

        val expected = files.take(ImportExportConstant.KeepBackupsNumber)
        useCase()
        val actual = getBackupsUseCase.invoke().map { it.file }
        assertContentEquals(expected, actual)
    }
}
