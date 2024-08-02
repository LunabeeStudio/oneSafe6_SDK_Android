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
 * Created by Lunabee Studio / Date - 11/28/2023 - for the oneSafe6 SDK.
 * Last modified 11/28/23, 12:21 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import javax.inject.Inject

private val logger = LBLogger.get<SetKeepLocalBackupUseCase>()

/**
 * Set KeepLocalBackupSettings option and remove locals backups if needed
 */
class SetKeepLocalBackupUseCase @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val localBackupRepository: LocalBackupRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(enabled: Boolean): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settings.setKeepLocalBackupSettings(safeId, enabled)
        if (!enabled) {
            localBackupRepository.deleteAll(safeId)
        }
    }
}
