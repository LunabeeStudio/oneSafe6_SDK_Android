/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.test.OSTestConfig
import java.time.Instant
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SecurityOptionModule::class],
)
internal object RepositoryTestModule {

    /**
     * [SecuritySettingsRepository] implementation with fixed value
     */
    @Singleton
    @Provides
    fun provideSecuritySettingsRepository(): SecuritySettingsRepository {
        return object : SecuritySettingsRepository {
            private var clipboardDelay: Duration = 10.seconds
            private var autoLockInactivityDelay = 30.seconds
            private var autoLockAppChangeDelay = 10.seconds
            private var autoLockOSKInactivityDelay = 30.seconds
            private var autoLockOSKHiddenDelay = 10.seconds
            private var lastPasswordVerif: Instant = Instant.now(OSTestConfig.clock)
            private var verifInterval = VerifyPasswordInterval.EVERY_MONTH
            private var bubblesResendMessageDelay = 1.days
            private var shakeToLock: Boolean = false

            override suspend fun autoLockInactivityDelay(safeId: SafeId): Duration {
                return autoLockInactivityDelay
            }

            override suspend fun autoLockAppChangeDelay(safeId: SafeId): Duration {
                return autoLockAppChangeDelay
            }

            override suspend fun clipboardClearDelay(safeId: SafeId): Duration {
                return clipboardDelay
            }

            override suspend fun verifyPasswordInterval(safeId: SafeId): VerifyPasswordInterval {
                return verifInterval
            }

            override suspend fun lastPasswordVerificationInstant(safeId: SafeId): Instant {
                return lastPasswordVerif
            }

            override suspend fun autoLockOSKInactivityDelay(safeId: SafeId): Duration {
                return autoLockOSKInactivityDelay
            }

            override suspend fun autoLockOSKHiddenDelay(safeId: SafeId): Duration {
                return autoLockOSKHiddenDelay
            }

            override fun autoLockInactivityDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(autoLockOSKInactivityDelay)
            }

            override fun autoLockAppChangeDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(autoLockAppChangeDelay)
            }

            override fun clipboardDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(clipboardDelay)
            }

            override fun verifyPasswordIntervalFlow(safeId: SafeId): Flow<VerifyPasswordInterval> {
                return flowOf(verifInterval)
            }

            override fun bubblesResendMessageDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(bubblesResendMessageDelay)
            }

            override fun autoLockOSKInactivityDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(autoLockOSKInactivityDelay)
            }

            override fun autoLockOSKHiddenDelayFlow(safeId: SafeId): Flow<Duration> {
                return flowOf(autoLockOSKHiddenDelay)
            }

            override fun shakeToLockFlow(safeId: SafeId): Flow<Boolean> {
                return flowOf(shakeToLock)
            }

            override suspend fun setAutoLockInactivityDelay(safeId: SafeId, delay: Duration) {
                autoLockInactivityDelay = delay
            }

            override suspend fun setAutoLockAppChangeDelay(safeId: SafeId, delay: Duration) {
                autoLockAppChangeDelay = delay
            }

            override suspend fun setClipboardClearDelay(safeId: SafeId, delay: Duration) {
                clipboardDelay = delay
            }

            override suspend fun setPasswordInterval(safeId: SafeId, passwordInterval: VerifyPasswordInterval) {
                verifInterval = passwordInterval
            }

            override suspend fun setLastPasswordVerification(safeId: SafeId, instant: Instant) {
                lastPasswordVerif = instant
            }

            override suspend fun setBubblesResendMessageDelay(safeId: SafeId, delay: Duration) {
                bubblesResendMessageDelay = delay
            }

            override suspend fun setAutoLockOSKInactivityDelay(safeId: SafeId, delay: Duration) {
                autoLockOSKInactivityDelay = delay
            }

            override suspend fun setAutoLockOSKHiddenDelay(safeId: SafeId, delay: Duration) {
                autoLockOSKHiddenDelay = delay
            }

            override suspend fun toggleShakeToLock(safeId: SafeId) {
                shakeToLock = !shakeToLock
            }
        }
    }
}
