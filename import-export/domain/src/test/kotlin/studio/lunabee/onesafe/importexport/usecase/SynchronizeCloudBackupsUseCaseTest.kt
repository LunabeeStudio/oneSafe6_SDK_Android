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
 * Created by Lunabee Studio / Date - 10/31/2023 - for the oneSafe6 SDK.
 * Last modified 10/31/23, 1:55 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.test.testClock
import java.io.File
import java.time.Instant

class SynchronizeCloudBackupsUseCaseTest {

    private val remoteMap = mutableMapOf<String, CloudBackup>()
    private val localMap = mutableMapOf<String, Pair<LocalBackup, Boolean>>() // boolean -> true if exist in remote

    private val cloudBackupRepository: CloudBackupRepository = mockk {
        coEvery { getBackups() } answers { remoteMap.values.toList() }

        every { refreshBackupList() } answers {
            flow {
                localMap += remoteMap.values.map {
                    it.id to (LocalBackup(it.date, File(it.name)) to true)
                }
                emit(LBFlowResult.Success(remoteMap.values.toList()))
            }
        }

        every { uploadBackup(backups = any()) } answers {
            flow {
                val backups = firstArg<List<LocalBackup>>()
                val cloudBackups = backups.map { CloudBackup(it.id, it.file.name, it.date) }
                remoteMap += cloudBackups.associateBy { it.remoteId }
                emit(LBFlowResult.Success(cloudBackups))
            }
        }

        every { deleteBackup(backups = any()) } answers {
            flow {
                firstArg<List<CloudBackup>>().forEach {
                    remoteMap.remove(it.remoteId)
                }
                emit(LBFlowResult.Success(Unit))
            }
        }
    }
    private val getLocalBackupsUseCase: GetLocalBackupsUseCase = mockk {
        coEvery { this@mockk.invoke(excludeRemote = true) } answers { localMap.values.filter { !it.second }.map { it.first } }
        coEvery { this@mockk.invoke(excludeRemote = false) } answers { localMap.values.toList().map { it.first } }
    }

    private val settings: AutoBackupSettingsRepository = mockk {
        every { keepLocalBackupEnabled } returns flowOf(true)
    }

    private val deleteOldCloudBackupsUseCase = DeleteOldCloudBackupsUseCase(cloudBackupRepository)

    private val useCase = SynchronizeCloudBackupsUseCase(cloudBackupRepository, getLocalBackupsUseCase, deleteOldCloudBackupsUseCase)

    @Test
    fun no_op_test(): TestResult = runTest {
        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) { cloudBackupRepository.uploadBackup(backups = emptyList()) }
        coVerify(exactly = 1) { cloudBackupRepository.deleteBackup(backups = emptyList()) }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun backup_to_upload_test(): TestResult = runTest {
        localMap += "to_upload_1" to (LocalBackup(Instant.now(testClock), File("to_upload_1")) to false)
        localMap += "to_upload_2" to (LocalBackup(Instant.now(testClock).plusMillis(1), File("to_upload_2")) to false)

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) {
            cloudBackupRepository.uploadBackup(
                backups = localMap.values.map { it.first }.sortedDescending(),
            )
        }
        coVerify(exactly = 1) { cloudBackupRepository.deleteBackup(backups = emptyList()) }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun backup_to_upload_over_limit_test(): TestResult = runTest {
        repeat(ImportExportConstant.KeepBackupsNumber + 3) {
            val id = "to_upload_$it"
            localMap += id to (LocalBackup(Instant.now(testClock), File(id)) to false)
        }

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) {
            cloudBackupRepository.uploadBackup(
                backups = localMap.values.map { it.first }.sortedDescending().take(ImportExportConstant.KeepBackupsNumber),
            )
        }
        coVerify(exactly = 1) { cloudBackupRepository.deleteBackup(backups = emptyList()) }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun backup_to_delete_test(): TestResult = runTest {
        val cloudBackups = List(ImportExportConstant.KeepBackupsNumber + 3) {
            val id = "to_delete_$it"
            CloudBackup(
                remoteId = id,
                name = id,
                date = Instant.now(testClock).plusMillis(it.toLong()),
            )
        }

        remoteMap += cloudBackups.map { it.id to it }

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.uploadBackup(backups = emptyList()) }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) {
            cloudBackupRepository.deleteBackup(
                backups = cloudBackups.sortedDescending().drop(ImportExportConstant.KeepBackupsNumber),
            )
        }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun backup_to_delete_after_upload_test(): TestResult = runTest {
        val now = Instant.now(testClock)
        val expectDelete = CloudBackup("to_delete", "to_delete", now)
        val expectUpload = buildList {
            this += LocalBackup(now.plusMillis(11), File("to_upload_0"))
            this += LocalBackup(now.plusMillis(8), File("to_upload_1"))
            this += LocalBackup(now.plusMillis(7), File("to_upload_2"))
        }

        remoteMap += "keep_0" to CloudBackup("keep_0", "keep_0", now.plusMillis(10))
        remoteMap += "keep_1" to CloudBackup("keep_1", "keep_0", now.plusMillis(9))
        remoteMap += "to_delete" to expectDelete

        expectUpload.forEach { localBackup ->
            localMap += localBackup.file.name to (localBackup to false)
        }
        localMap += "not_to_upload" to (LocalBackup(now.plusMillis(6), File("not_to_upload")) to false)

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) { cloudBackupRepository.uploadBackup(backups = expectUpload) }
        coVerify(exactly = 1) { cloudBackupRepository.deleteBackup(backups = listOf(expectDelete)) }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun delete_local_backup_on_success_test(): TestResult = runTest {
        every { settings.keepLocalBackupEnabled } returns flowOf(false)

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.getBackups() }
        coVerify(exactly = 1) { cloudBackupRepository.uploadBackup(backups = emptyList()) }
        coVerify(exactly = 1) { cloudBackupRepository.deleteBackup(backups = emptyList()) }

        confirmVerified(cloudBackupRepository)
    }

    @Test
    fun do_not_delete_local_backup_on_failure_test(): TestResult = runTest {
        every { settings.keepLocalBackupEnabled } returns flowOf(false)
        every { cloudBackupRepository.uploadBackup(backups = any()) } returns flowOf(LBFlowResult.Failure())

        useCase.invoke().last()

        coVerify(exactly = 1) { cloudBackupRepository.refreshBackupList() }
        coVerify(exactly = 1) { cloudBackupRepository.uploadBackup(backups = emptyList()) }

        confirmVerified(cloudBackupRepository)
    }
}
