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
 * Created by Lunabee Studio / Date - 9/21/2023 - for the oneSafe6 SDK.
 * Last modified 9/21/23, 10:50 AM
 */

package studio.lunabee.onesafe.ime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.RefreshLastUserInteractionUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.ime.model.OSKImeState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class OSKAutoLockInactivityManager @Inject constructor(
    private val autoLockInactivityUseCase: AutoLockInactivityUseCase,
    private val getSecuritySettingUseCase: GetSecuritySettingUseCase,
    private val refreshLastUserInteractionUseCase: RefreshLastUserInteractionUseCase,
) : OSKImeStateObserver {
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var inactivityJob: Job? = null

    override suspend fun onStateChange(state: OSKImeState) {
        val autoLockOSKInactivityDelay = getSecuritySettingUseCase.autoLockOSKInactivityDelay()
        if (autoLockOSKInactivityDelay.data != Duration.INFINITE) {
            when (state) {
                OSKImeState.Hidden,
                OSKImeState.Keyboard,
                -> stopAutoLockInactivity()
                OSKImeState.ScreenWithKeyboard,
                OSKImeState.Screen,
                -> startAutoLockInactivity()
            }
        }
    }

    fun refreshLastUserInteraction() {
        refreshLastUserInteractionUseCase()
    }

    private fun startAutoLockInactivity() {
        inactivityJob?.cancel()
        inactivityJob = coroutineScope.launch {
            autoLockInactivityUseCase.osk()
        }
    }

    private fun stopAutoLockInactivity() {
        inactivityJob?.cancel()
    }
}
