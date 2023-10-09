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

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.messaging.repository.repository.ConversationRepositoryImpl
import studio.lunabee.messaging.repository.repository.HandShakeDataRepositoryImpl
import studio.lunabee.messaging.repository.repository.MessageChannelRepositoryImpl
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.PasswordGeneratorConfigRepository
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.messaging.domain.repository.ConversationRepository
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.repository.repository.ForceUpgradeRepositoryImpl
import studio.lunabee.onesafe.repository.repository.IndexWordEntryRepositoryImpl
import studio.lunabee.onesafe.repository.repository.PasswordGeneratorConfigRepositoryImpl
import studio.lunabee.onesafe.repository.repository.RecentSearchRepositoryImpl
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class, SecurityOptionModule::class, RepositoryServiceModule::class],
)
interface RepositoryTestModule {
    @Binds
    fun bindIndexWordEntryRepository(indexWordEntryRepository: IndexWordEntryRepositoryImpl): IndexWordEntryRepository

    @Binds
    fun bindForceUpgradeRepository(forceUpgradeRepositoryImpl: ForceUpgradeRepositoryImpl): ForceUpgradeRepository

    @Binds
    fun bindsRecentSearchRepository(recentSearchRepositoryImpl: RecentSearchRepositoryImpl): RecentSearchRepository

    @Binds
    fun bindsPasswordGeneratorConfigRepository(
        passwordGeneratorConfigRepositoryImpl: PasswordGeneratorConfigRepositoryImpl,
    ): PasswordGeneratorConfigRepository

    @Binds
    fun bindsMessageChannelRepository(messageChannelRepositoryImpl: MessageChannelRepositoryImpl): MessageChannelRepository

    @Binds
    fun bindsHandShakeDataRepository(handShakeDataRepositoryImpl: HandShakeDataRepositoryImpl): HandShakeDataRepository

    @Binds
    fun bindsConversationRepository(conversationRepositoryImpl: ConversationRepositoryImpl): ConversationRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object TestSecurityOptionModule {

    /**
     * [SecurityOptionRepository] implementation with fixed value
     */
    @Singleton
    @Provides
    fun provideSecurityOptionRepository(): SecurityOptionRepository {
        return object : SecurityOptionRepository {
            private var internalClipboardDelay: Duration = 10.seconds
            private var internalAutoLockInactivityDelay = 30.seconds
            private var internalAutoLockAppChangeDelay = 10.seconds
            private var internalAutoLockOSKInactivityDelay = 30.seconds
            private var internalAutoLockOSKHiddenDelay = 10.seconds
            private var lastPasswordVerif: Long? = null
            private var verifInterval = VerifyPasswordInterval.EVERY_MONTH

            override val autoLockInactivityDelay: Duration
                get() = internalAutoLockInactivityDelay

            override val autoLockInactivityDelayFlow: Flow<Duration>
                get() = flowOf(internalAutoLockInactivityDelay)

            override fun setAutoLockInactivityDelay(delay: Duration) {
                internalAutoLockInactivityDelay = delay
            }

            override val autoLockAppChangeDelay: Duration
                get() = internalAutoLockAppChangeDelay

            override val autoLockAppChangeDelayFlow: Flow<Duration>
                get() = flowOf(internalAutoLockAppChangeDelay)

            override fun setAutoLockAppChangeDelay(delay: Duration) {
                internalAutoLockAppChangeDelay = delay
            }

            override val clipboardDelay
                get() = internalClipboardDelay
            override val clipboardDelayFlow: Flow<Duration>
                get() = flowOf(clipboardDelay)

            override fun setClipboardClearDelay(delay: Duration) {
                internalClipboardDelay = delay
            }

            override val verifyPasswordInterval: VerifyPasswordInterval
                get() = verifInterval

            override val lastPasswordVerificationTimeStamp: Long?
                get() = lastPasswordVerif

            override val verifyPasswordIntervalFlow: Flow<VerifyPasswordInterval>
                get() = flowOf(verifInterval)

            override fun setLastPasswordVerification(timeStamp: Long) {
                lastPasswordVerif = timeStamp
            }

            override val bubblesResendMessageDelayFlow: Flow<Duration>
                get() = flowOf(1.days)

            override fun setBubblesResendMessageDelay(delay: Duration) {}
            override val autoLockOSKInactivityDelay: Duration
                get() = internalAutoLockOSKInactivityDelay
            override val autoLockOSKInactivityDelayFlow: Flow<Duration>
                get() = flowOf(internalAutoLockOSKInactivityDelay)

            override fun setAutoLockOSKInactivityDelay(delay: Duration) {
                internalAutoLockOSKInactivityDelay = delay
            }

            override val autoLockOSKHiddenDelay: Duration
                get() = internalAutoLockOSKHiddenDelay

            override val autoLockOSKHiddenDelayFlow: Flow<Duration>
                get() = flowOf(internalAutoLockOSKHiddenDelay)

            override fun setAutoLockOSKHiddenDelay(delay: Duration) {
                internalAutoLockOSKHiddenDelay = delay
            }

            override fun setPasswordInterval(passwordInterval: VerifyPasswordInterval) {
                verifInterval = passwordInterval
            }
        }
    }
}
