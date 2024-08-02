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

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import javax.inject.Inject

private val log = LBLogger.get<DeleteOldCloudBackupsUseCase>()

/**
 * Delete oldest backups from cloud to keep only the number required by the user
 */
class DeleteOldCloudBackupsUseCase @Inject constructor(
    private val backupRepository: CloudBackupRepository,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
) {
    /**
     * Refresh and delete old cloud backups
     */
    operator fun invoke(safeId: SafeId): Flow<LBFlowResult<Unit>> {
        return backupRepository.refreshBackupList(safeId).transformResult { success ->
            emitAll(invoke(safeId, success.successData))
        }
    }

    /**
     * Delete old cloud backups
     */
    suspend operator fun invoke(safeId: SafeId, allBackups: List<CloudBackup>): Flow<LBFlowResult<Unit>> {
        val backupsToDelete = allBackups
            .filter { it.safeId != null }
            .sortedDescending()
            .drop(autoBackupSettingsRepository.autoBackupMaxNumber(safeId))
        log.v("Found ${backupsToDelete.size} to delete")
        return backupRepository.deleteBackup(backupsToDelete)
    }
}
