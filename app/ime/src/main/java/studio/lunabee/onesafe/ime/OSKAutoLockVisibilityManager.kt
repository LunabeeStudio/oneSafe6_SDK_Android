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
 * Created by Lunabee Studio / Date - 9/20/2023 - for the oneSafe6 SDK.
 * Last modified 9/20/23, 2:10 PM
 */

package studio.lunabee.onesafe.ime

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockBackgroundUseCase
import studio.lunabee.onesafe.ime.model.OSKImeState
import javax.inject.Inject
import kotlin.time.Duration

class OSKAutoLockVisibilityManager @Inject constructor(
    private val autoLockBackgroundUseCase: AutoLockBackgroundUseCase,
    private val securityOptionRepository: SecurityOptionRepository,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) : CloseableCoroutineScope by CloseableMainCoroutineScope(), OSKImeStateObserver {

    private var oskVisibilityChangeJob: Job? = null

    override fun onStateChange(state: OSKImeState) {
        if (securityOptionRepository.autoLockOSKHiddenDelay != Duration.INFINITE) {
            when (state) {
                OSKImeState.Hidden,
                OSKImeState.Keyboard,
                -> launchLock()
                OSKImeState.ScreenWithKeyboard,
                OSKImeState.Screen,
                -> cancelLock()
            }
        }
    }

    private fun launchLock() {
        coroutineScope.launch {
            if (isCryptoDataReadyInMemoryUseCase().first() && oskVisibilityChangeJob == null || oskVisibilityChangeJob?.isActive == false) {
                oskVisibilityChangeJob = coroutineScope.launch {
                    autoLockBackgroundUseCase.osk()
                }
            }
        }
    }

    private fun cancelLock() {
        oskVisibilityChangeJob?.cancel()
        oskVisibilityChangeJob = null
    }
}
