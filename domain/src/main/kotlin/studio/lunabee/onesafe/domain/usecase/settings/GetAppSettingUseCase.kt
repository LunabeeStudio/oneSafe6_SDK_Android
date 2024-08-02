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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 5:39 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Get an app setting if the safe id is loaded, or fallback to the default value with failure result
 */
class GetAppSettingUseCase @Inject constructor(
    private val settingRepository: SafeSettingsRepository,
    private val safeRepository: SafeRepository,
    getDefaultSafeSettingsProvider: DefaultSafeSettingsProvider,
) {
    private val default = getDefaultSafeSettingsProvider()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun materialYou(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.materialYou(safeId)
        } ?: flowOf(default.materialYou)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun automationFlow(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.automationFlow(safeId)
        } ?: flowOf(default.automation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun displayShareWarningFlow(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.displayShareWarningFlow(safeId)
        } ?: flowOf(default.displayShareWarning)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun cameraSystemFlow(): Flow<CameraSystem> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.cameraSystemFlow(safeId)
        } ?: flowOf(default.cameraSystem)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun allowScreenshotFlow(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.allowScreenshotFlow(safeId)
        } ?: flowOf(default.allowScreenshot)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun bubblesPreview(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.bubblesPreview(safeId)
        } ?: flowOf(default.bubblesPreview)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun bubblesHomeCardCtaState(): Flow<CtaState> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.bubblesHomeCardCtaState(safeId)
        } ?: flowOf(default.bubblesHomeCardCtaState)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun independentSafeInfoCtaState(): Flow<CtaState> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            settingRepository.independentSafeInfoCtaState(safeId)
        } ?: flowOf(default.independentSafeInfoCtaState)
    }

    suspend fun cameraSystem(): LBResult<CameraSystem> = OSError.runCatching(
        failureData = { default.cameraSystem },
    ) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.cameraSystem(safeId)
    }

    suspend fun allowScreenshot(): LBResult<Boolean> = OSError.runCatching(
        failureData = { default.allowScreenshot },
    ) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.allowScreenshot(safeId)
    }

    suspend fun automation(): LBResult<Boolean> = OSError.runCatching(
        failureData = { default.automation },
    ) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.automation(safeId)
    }

    suspend fun displayShareWarning(): LBResult<Boolean> = OSError.runCatching(
        failureData = { default.displayShareWarning },
    ) {
        val safeId = safeRepository.currentSafeId()
        settingRepository.displayShareWarning(safeId)
    }
}
