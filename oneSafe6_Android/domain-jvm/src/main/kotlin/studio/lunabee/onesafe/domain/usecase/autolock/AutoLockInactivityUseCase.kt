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

import co.touchlab.kermit.Logger
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject
import kotlin.time.Duration

private val logger: Logger = LBLogger.get<AutoLockInactivityUseCase>()

class AutoLockInactivityUseCase @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val autoLockInactivityGetRemainingTimeUseCase: AutoLockInactivityGetRemainingTimeUseCase,
    private val lockAppUseCase: LockAppUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    private val safeRepository: SafeRepository,
) {

    suspend fun app(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        doAutoLock(
            inactivityDelayFlow = securitySettingsRepository.autoLockInactivityDelayFlow(safeId),
            remainingDelay = { autoLockInactivityGetRemainingTimeUseCase.app(safeId) },
            clearClipboard = true,
        )
    }

    suspend fun osk(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        doAutoLock(
            inactivityDelayFlow = securitySettingsRepository.autoLockOSKInactivityDelayFlow(safeId),
            remainingDelay = { autoLockInactivityGetRemainingTimeUseCase.osk(safeId) },
            clearClipboard = false,
        )
    }

    private suspend fun doAutoLock(
        inactivityDelayFlow: Flow<Duration>,
        remainingDelay: suspend () -> Duration,
        clearClipboard: Boolean,
    ) {
        combine(
            inactivityDelayFlow,
            isSafeReadyUseCase.flow(),
        ) { inactivityDelay, isCryptoDataReady ->
            inactivityDelay to isCryptoDataReady
        }.collectLatest { (inactivityDelay, isCryptoDataReady) ->
            if (isCryptoDataReady && inactivityDelay != Duration.INFINITE) {
                var currentDelay = inactivityDelay
                while (currentDelay > Duration.ZERO) {
                    delay(currentDelay)
                    currentDelay = remainingDelay()
                }
                lockAppUseCase(clearClipboard)
            }
        }
    }
}
