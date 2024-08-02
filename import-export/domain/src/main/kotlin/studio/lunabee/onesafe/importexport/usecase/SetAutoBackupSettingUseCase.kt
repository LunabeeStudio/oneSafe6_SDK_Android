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
 * Created by Lunabee Studio / Date - 6/26/2024 - for the oneSafe6 SDK.
 * Last modified 6/26/24, 8:24 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<SetAutoBackupSettingUseCase>()

class SetAutoBackupSettingUseCase @Inject constructor(
    private val settingsRepository: AutoBackupSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    suspend fun toggleAutoBackupSettings(): LBResult<Boolean> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.toggleAutoBackupSettings(safeId)
    }

    suspend fun setAutoBackupFrequency(delay: Duration): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setAutoBackupFrequency(safeId, delay)
    }

    suspend fun updateAutoBackupMaxNumber(updatedValue: Int): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.updateAutoBackupMaxNumber(safeId, updatedValue)
    }

    suspend fun setCloudBackupEnabled(enabled: Boolean): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setCloudBackupEnabled(safeId, enabled)
    }

    suspend fun setKeepLocalBackupSettings(enabled: Boolean): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setKeepLocalBackupSettings(safeId, enabled)
    }

    suspend fun setEnableAutoBackupCtaState(ctaState: CtaState): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingsRepository.setEnableAutoBackupCtaState(safeId, ctaState)
    }
}
