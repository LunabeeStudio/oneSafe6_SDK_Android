/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 3/25/2024 - for the oneSafe6 SDK.
 * Last modified 3/25/24, 9:05 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import javax.inject.Inject

/**
 * Clear the auto-backup stored error according to the current backup settings and the source of the error.
 *   • Do not clear the error if a different type of backup succeed. For example if cloud backup has stored an error, do not clear it if
 *   local backup succeed.
 *   • Always clear the error if the backup mode has changed. For example if cloud backup has stored an error, then user switch has switched
 *   off the cloud backup, and a local backup succeed.
 */
class ClearAutoBackupErrorUseCase @Inject constructor(
    private val autoBackupErrorRepository: AutoBackupErrorRepository,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
) {
    /**
     * Clear the error if needed
     *
     * @see ClearAutoBackupErrorUseCase
     */
    suspend fun ifNeeded(backupMode: AutoBackupMode) {
        val currentBackupMode = getAutoBackupModeUseCase()
        val shouldClear =
            // Error match the current setting
            backupMode == currentBackupMode ||
                // Backup are disabled
                currentBackupMode == AutoBackupMode.Disabled ||
                // Error match the current error
                backupMode == autoBackupErrorRepository.getError().firstOrNull()?.source
        if (shouldClear) autoBackupErrorRepository.setError(null)
    }

    /**
     * Force clear the error
     */
    suspend fun force() {
        autoBackupErrorRepository.setError(null)
    }
}
