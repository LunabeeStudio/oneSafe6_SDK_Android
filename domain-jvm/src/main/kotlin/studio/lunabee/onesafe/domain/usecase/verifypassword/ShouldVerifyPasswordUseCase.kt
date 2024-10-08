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
 * Created by Lunabee Studio / Date - 4/21/2023 - for the oneSafe6 SDK.
 * Last modified 4/21/23, 5:15 PM
 */

package studio.lunabee.onesafe.domain.usecase.verifypassword

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

private val logger = LBLogger.get<ShouldVerifyPasswordUseCase>()

class ShouldVerifyPasswordUseCase @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(): LBResult<Boolean> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        val isCurrentSafeBiometricEnabled = isCurrentSafeBiometricEnabledUseCase()
        val lastPasswordVerification = securitySettingsRepository.lastPasswordVerificationInstant(safeId)
        val verificationInterval = securitySettingsRepository.verifyPasswordInterval(safeId)
        var shouldVerifyDateTime = LocalDateTime.ofInstant(lastPasswordVerification, ZoneId.systemDefault())
        shouldVerifyDateTime = when (verificationInterval) {
            VerifyPasswordInterval.EVERY_WEEK -> shouldVerifyDateTime.plusWeeks(1)
            VerifyPasswordInterval.EVERY_TWO_WEEKS -> shouldVerifyDateTime.plusWeeks(2)
            VerifyPasswordInterval.EVERY_MONTH -> shouldVerifyDateTime.plusMonths(1)
            VerifyPasswordInterval.EVERY_TWO_MONTHS -> shouldVerifyDateTime.plusMonths(2)
            VerifyPasswordInterval.EVERY_SIX_MONTHS -> shouldVerifyDateTime.plusMonths(6)
            VerifyPasswordInterval.NEVER -> LocalDateTime.MAX
        }
        isCurrentSafeBiometricEnabled && shouldVerifyDateTime.isBefore(LocalDateTime.now(clock))
    }
}
