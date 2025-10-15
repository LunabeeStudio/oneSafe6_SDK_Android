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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 10:32 AM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<SetAppSettingUseCase>()

class SetAppSettingUseCase @Inject constructor(
    private val settingRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
) {
    suspend fun toggleMaterialYou(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.toggleMaterialYou(safeId)
    }

    suspend fun toggleAutomation(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.toggleAutomation(safeId)
    }

    suspend fun disableShareWarningDisplay(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.disableShareWarningDisplay(safeId)
    }

    suspend fun toggleAllowScreenshot(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.toggleAllowScreenshot(safeId)
    }

    suspend fun setBubblesPreview(value: Boolean): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.setBubblesPreview(safeId, value)
    }

    suspend fun setCameraSystem(value: CameraSystem): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.setCameraSystem(safeId, value)
    }

    suspend fun setBubblesHomeCardCtaState(ctaState: CtaState): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.setBubblesHomeCardCtaState(safeId, ctaState)
    }
}
