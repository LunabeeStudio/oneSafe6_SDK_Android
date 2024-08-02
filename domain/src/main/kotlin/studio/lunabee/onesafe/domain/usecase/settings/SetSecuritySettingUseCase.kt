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
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<SetSecuritySettingUseCase>()

/**
 * Retrieve individual safe security setting as a flow or as a [LBResult]
 */
class SetSecuritySettingUseCase @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val safeRepository: SafeRepository,
    private val clock: Clock,
) {
    suspend fun setAutoLockInactivityDelay(delay: Duration, currentSafeId: SafeId? = null): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setAutoLockInactivityDelay(safeId, delay)
    }

    suspend fun setAutoLockAppChangeDelay(delay: Duration, currentSafeId: SafeId? = null): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setAutoLockAppChangeDelay(safeId, delay)
    }

    suspend fun setClipboardClearDelay(delay: Duration, currentSafeId: SafeId? = null): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setClipboardClearDelay(safeId, delay)
    }

    suspend fun setVerifyPasswordInterval(
        passwordInterval: VerifyPasswordInterval,
        currentSafeId: SafeId? = null,
    ): LBResult<Unit> = OSError.runCatching(
        logger,
    ) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setPasswordInterval(safeId, passwordInterval)
    }

    suspend fun setLastPasswordVerification(
        instant: Instant = Instant.now(clock),
        currentSafeId: SafeId? = null,
    ): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setLastPasswordVerification(safeId, instant)
    }

    suspend fun setBubblesResendMessageDelay(delay: Duration, currentSafeId: SafeId? = null): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setBubblesResendMessageDelay(safeId, delay)
    }

    suspend fun setAutoLockOSKInactivityDelay(
        delay: Duration,
        currentSafeId: SafeId? = null,
    ): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setAutoLockOSKInactivityDelay(safeId, delay)
    }

    suspend fun setAutoLockOSKHiddenDelay(delay: Duration, currentSafeId: SafeId? = null): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.setAutoLockOSKHiddenDelay(safeId, delay)
    }

    suspend fun toggleShakeToLock(currentSafeId: SafeId? = null) {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.toggleShakeToLock(safeId)
    }
}
