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
 * Created by Lunabee Studio / Date - 4/11/2023 - for the oneSafe6 SDK.
 * Last modified 4/11/23, 10:45 AM
 */

package studio.lunabee.onesafe.domain.usecase.autolock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import javax.inject.Inject
import kotlin.time.Duration

class AutoLockInactivityUseCase @Inject constructor(
    private val securityOptionRepository: SecurityOptionRepository,
    private val autoLockInactivityGetRemainingTimeUseCase: AutoLockInactivityGetRemainingTimeUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) {

    suspend fun app() {
        doAutoLock(
            securityOptionRepository.autoLockInactivityDelayFlow,
            autoLockInactivityGetRemainingTimeUseCase::app,
        )
    }

    suspend fun osk() {
        doAutoLock(
            securityOptionRepository.autoLockOSKInactivityDelayFlow,
            autoLockInactivityGetRemainingTimeUseCase::osk,
        )
    }

    private suspend fun doAutoLock(
        inactivityDelayFlow: Flow<Duration>,
        remainingDelay: () -> Duration,
    ) {
        combine(
            inactivityDelayFlow,
            isCryptoDataReadyInMemoryUseCase(),
        ) { inactivityDelay, isCryptoDataReady ->
            inactivityDelay to isCryptoDataReady
        }.collectLatest { (inactivityDelay, isCryptoDataReady) ->
            if (isCryptoDataReady && inactivityDelay != Duration.INFINITE) {
                var currentDelay = inactivityDelay
                while (currentDelay > Duration.ZERO) {
                    delay(currentDelay)
                    currentDelay = remainingDelay()
                }
                lockAppUseCase()
            }
        }
    }
}
