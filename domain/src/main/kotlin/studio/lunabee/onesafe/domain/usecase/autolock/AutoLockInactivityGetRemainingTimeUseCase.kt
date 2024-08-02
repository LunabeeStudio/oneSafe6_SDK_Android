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

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

interface AutoLockInactivityGetRemainingTimeUseCase {
    suspend fun app(currentSafeId: SafeId? = null): Duration

    suspend fun osk(currentSafeId: SafeId? = null): Duration
}

class AutoLockInactivityGetRemainingTimeUseCaseImpl @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val autoLockRepository: AutoLockRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) : AutoLockInactivityGetRemainingTimeUseCase {
    override suspend fun app(currentSafeId: SafeId?): Duration {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        val inactivityDelay = securitySettingsRepository.autoLockInactivityDelay(safeId)
        return computeRemainingTime(inactivityDelay)
    }

    override suspend fun osk(currentSafeId: SafeId?): Duration {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        val inactivityDelay = securitySettingsRepository.autoLockOSKInactivityDelay(safeId)
        return computeRemainingTime(inactivityDelay)
    }

    private fun computeRemainingTime(inactivityDelay: Duration): Duration {
        val lastUserInteraction = autoLockRepository.lastUserInteractionInstant
        val currentInactivity = JavaDuration.between(lastUserInteraction, Instant.now(clock)).toKotlinDuration()
        return maxOf(inactivityDelay - currentInactivity, Duration.ZERO)
    }
}
