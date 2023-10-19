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
 * Last modified 10/2/23, 8:54 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import javax.inject.Inject

private val log = LBLogger.get<GetLocalBackupsUseCase>()

/**
 * Retrieve all internal backups ordered by date (latest first).
 */
class GetLocalBackupsUseCase @Inject constructor(
    private val backupRepository: LocalBackupRepository,
) {
    /**
     * Get all valid local backups and remove (+log) potential broken backups (backup file missing)
     */
    suspend operator fun invoke(): List<LocalBackup> = handleResult(backupRepository.getBackups())

    /**
     * Get a flow of all valid local backups and remove (+log) potential broken backups (backup file missing)
     */
    fun flow(): Flow<List<LocalBackup>> = backupRepository.getBackupsFlow().map(::handleResult)

    private suspend fun handleResult(backupResults: List<LBResult<LocalBackup>>): List<LocalBackup> {
        val failures = backupResults
            .filterIsInstance<LBResult.Failure<LocalBackup>>()
            .onEach {
                log.e(it.throwable)
            }
        if (failures.isNotEmpty()) {
            val brokenBackups = failures.mapNotNull {
                if (((it as? LBResult.Failure)?.throwable as? OSStorageError)?.code == OSStorageError.Code.MISSING_BACKUP_FILE) {
                    it.failureData
                } else {
                    null
                }
            }
            backupRepository.delete(brokenBackups)
        }

        return backupResults
            .filterIsInstance<LBResult.Success<LocalBackup>>()
            .map { it.successData }
    }
}
