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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import studio.lunabee.bubbles.repository.repository.ContactKeyRepositoryImpl
import studio.lunabee.bubbles.repository.repository.ContactRepositoryImpl
import studio.lunabee.messaging.repository.repository.EnqueuedMessageRepositoryImpl
import studio.lunabee.messaging.repository.repository.HandShakeDataRepositoryImpl
import studio.lunabee.messaging.repository.repository.MessageChannelRepositoryImpl
import studio.lunabee.messaging.repository.repository.MessageOrderRepositoryImpl
import studio.lunabee.messaging.repository.repository.MessageRepositoryImpl
import studio.lunabee.messaging.repository.repository.SentMessageRepositoryImpl
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
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
import studio.lunabee.onesafe.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageOrderRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
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
import studio.lunabee.onesafe.repository.repository.SecurityOptionRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SupportOSRepositoryImpl
import studio.lunabee.onesafe.repository.repository.UrlMetadataRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
interface RepositoryModule {

    @Binds
    @ActivityRetainedScoped
    fun bindIndexWordEntryRepository(indexWordEntryRepository: IndexWordEntryRepositoryImpl): IndexWordEntryRepository

    @Binds
    @ActivityRetainedScoped
    fun bindForceUpgradeRepository(forceUpgradeRepositoryImpl: ForceUpgradeRepositoryImpl): ForceUpgradeRepository

    @Binds
    @ActivityRetainedScoped
    fun bindsRecentSearchRepository(recentSearchRepositoryImpl: RecentSearchRepositoryImpl): RecentSearchRepository

    @Binds
    @ActivityRetainedScoped
    fun bindsPasswordGeneratorConfigRepository(
        passwordGeneratorConfigRepositoryImpl: PasswordGeneratorConfigRepositoryImpl,
    ): PasswordGeneratorConfigRepository

    @Binds
    @ActivityRetainedScoped
    fun bindsMessageChannelRepository(
        messageChannelRepositoryImpl: MessageChannelRepositoryImpl,
    ): MessageChannelRepository

    @Binds
    fun bindsHandShakeDataRepository(handShakeDataRepositoryImpl: HandShakeDataRepositoryImpl): HandShakeDataRepository
}

@Module
@InstallIn(ServiceComponent::class)
interface RepositoryServiceModule {
    @Binds
    @ServiceScoped
    fun bindsMessageChannelRepository(
        messageChannelRepositoryImpl: MessageChannelRepositoryImpl,
    ): MessageChannelRepository

    @Binds
    fun bindsHandShakeDataRepository(handShakeDataRepositoryImpl: HandShakeDataRepositoryImpl): HandShakeDataRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryGlobalModule {
    @Binds
    fun bindUrlMetadataRepository(urlMetadataRepository: UrlMetadataRepositoryImpl): UrlMetadataRepository

    @Binds
    fun bindSecurityOptionRepository(securityOptionRepository: SecurityOptionRepositoryImpl): SecurityOptionRepository

    @Binds
    fun bindClipboardRepository(clipboardRepository: ClipboardRepositoryImpl): ClipboardRepository

    @Binds
    fun bindAutoLockRepository(autoLockRepositoryImpl: AutoLockRepositoryImpl): AutoLockRepository

    @Binds
    fun bindSafeItemRepository(safeItemRepository: SafeItemRepositoryImpl): SafeItemRepository

    @Binds
    fun bindSafeItemDeletedRepository(safeItemDeletedRepository: SafeItemDeletedRepositoryImpl): SafeItemDeletedRepository

    @Binds
    fun bindSafeItemKeyRepository(safeItemKeyRepository: SafeItemKeyRepositoryImpl): SafeItemKeyRepository

    @Binds
    fun bindSafeItemFieldRepository(safeItemFieldRepository: SafeItemFieldRepositoryImpl): SafeItemFieldRepository

    @Binds
    fun bindIconRepository(iconRepository: IconRepositoryImpl): IconRepository

    @Binds
    fun bindSupportOSRepository(supportOSRepository: SupportOSRepositoryImpl): SupportOSRepository

    @Binds
    fun bindsContactRepository(bubblesContactRepositoryImpl: ContactRepositoryImpl): ContactRepository

    @Binds
    fun bindsContactKeyRepository(bubblesContactKeyRepositoryImpl: ContactKeyRepositoryImpl): ContactKeyRepository

    @Binds
    fun bindsMessageRepository(bubblesMessageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    fun bindsSentMessageRepository(bubblesSentMessageRepositoryImpl: SentMessageRepositoryImpl): SentMessageRepository

    @Binds
    fun bindsMessageOrderRepository(bubblesMessageOrderRepositoryImpl: MessageOrderRepositoryImpl): MessageOrderRepository

    @Binds
    fun bindsQueueMessageRepository(queueMessageRepositoryImpl: EnqueuedMessageRepositoryImpl): EnqueuedMessageRepository
}
