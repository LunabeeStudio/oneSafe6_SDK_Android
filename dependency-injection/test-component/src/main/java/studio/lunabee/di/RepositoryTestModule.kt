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
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.PasswordGeneratorConfigRepository
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.repository.repository.AutoLockRepositoryImpl
import studio.lunabee.onesafe.repository.repository.ClipboardRepositoryImpl
import studio.lunabee.onesafe.repository.repository.ForceUpgradeRepositoryImpl
import studio.lunabee.onesafe.repository.repository.IconRepositoryImpl
import studio.lunabee.onesafe.repository.repository.IndexWordEntryRepositoryImpl
import studio.lunabee.onesafe.repository.repository.PasswordGeneratorConfigRepositoryImpl
import studio.lunabee.onesafe.repository.repository.RecentSearchRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemDeletedRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemFieldRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemKeyRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SupportOSRepositoryImpl
import studio.lunabee.onesafe.repository.repository.UrlMetadataRepositoryImpl
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class, RepositoryGlobalModule::class],
)
interface RepositoryTestModule {
    @Binds
    @Singleton
    fun bindUrlMetadataRepository(urlMetadataRepository: UrlMetadataRepositoryImpl): UrlMetadataRepository

    @Binds
    @Singleton
    fun bindIndexWordEntryRepository(indexWordEntryRepository: IndexWordEntryRepositoryImpl): IndexWordEntryRepository

    @Binds
    @Singleton
    fun bindForceUpgradeRepository(forceUpgradeRepositoryImpl: ForceUpgradeRepositoryImpl): ForceUpgradeRepository

    @Binds
    @Singleton
    fun bindsRecentSearchRepository(recentSearchRepositoryImpl: RecentSearchRepositoryImpl): RecentSearchRepository

    @Binds
    @Singleton
    fun bindsPasswordGeneratorConfigRepository(
        passwordGeneratorConfigRepositoryImpl: PasswordGeneratorConfigRepositoryImpl,
    ): PasswordGeneratorConfigRepository

    @Binds
    @Singleton
    fun bindClipboardRepository(clipboardRepository: ClipboardRepositoryImpl): ClipboardRepository

    @Binds
    @Singleton
    fun bindSafeItemRepository(safeItemRepository: SafeItemRepositoryImpl): SafeItemRepository

    @Binds
    @Singleton
    fun bindSafeItemDeletedRepository(safeItemDeletedRepository: SafeItemDeletedRepositoryImpl): SafeItemDeletedRepository

    @Binds
    @Singleton
    fun bindSafeItemKeyRepository(safeItemKeyRepository: SafeItemKeyRepositoryImpl): SafeItemKeyRepository

    @Binds
    @Singleton
    fun bindSafeItemFieldRepository(safeItemFieldRepository: SafeItemFieldRepositoryImpl): SafeItemFieldRepository

    @Binds
    @Singleton
    fun bindIconRepository(iconRepository: IconRepositoryImpl): IconRepository

    @Binds
    @Singleton
    fun bindsAutoLockRepository(autoLockRepositoryImpl: AutoLockRepositoryImpl): AutoLockRepository

    @Binds
    @Singleton
    fun bindSupportOSRepository(supportOSRepository: SupportOSRepositoryImpl): SupportOSRepository
}

@Module
@InstallIn(SingletonComponent::class)
internal object SecurityOptionModule {

    /**
     * Fix current clipboard delay
     */
    @Provides
    @Singleton
    fun provideSecurityOptionRepository(): SecurityOptionRepository {
        return object : SecurityOptionRepository {
            private var internalClipboardDelay: Duration = 10.seconds
            private var internalAutoLockInactivityDelay = 30.seconds
            private var internalAutoLockAppChangeDelay = 10.seconds
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

            override fun setPasswordInterval(passwordInterval: VerifyPasswordInterval) {
                verifInterval = passwordInterval
            }
        }
    }
}
