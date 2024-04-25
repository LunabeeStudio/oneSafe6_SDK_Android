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
 * Created by Lunabee Studio / Date - 10/30/2023 - for the oneSafe6 SDK.
 * Last modified 10/30/23, 5:25 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import javax.inject.Inject

private val log = LBLogger.get<SynchronizeCloudBackupsUseCase>()

/**
 * Synchronize cloud backup DB & remote storage
 *
 * 1. Refresh cloud backup list
 * 2. Upload the latest locals backups
 * 3. Delete oldest cloud backups
 */
class SynchronizeCloudBackupsUseCase @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository,
    private val getAllLocalBackupsUseCase: GetAllLocalBackupsUseCase,
    private val deleteOldCloudBackupsUseCase: DeleteOldCloudBackupsUseCase,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
) {
    operator fun invoke(): Flow<LBFlowResult<Unit>> {
        // 1. Refresh cloud backup list
        return cloudBackupRepository.refreshBackupList()
            .transformResult { backupsResult ->
                val backupsToUpload = (backupsResult.successData + getAllLocalBackupsUseCase(true))
                    .sortedDescending()
                    .take(autoBackupSettingsRepository.autoBackupMaxNumber)
                    .filterIsInstance<LocalBackup>()
                log.v("Found ${backupsToUpload.size} to upload")

                // 2. Upload the latest locals backups
                val uploadAndDeleteFlow = cloudBackupRepository.uploadBackup(backupsToUpload)
                    .transformResult {
                        // 3. Delete oldest cloud backups
                        emitAll(deleteOldCloudBackupsUseCase(cloudBackupRepository.getBackups()))
                    }

                emitAll(uploadAndDeleteFlow)
            }
    }
}
