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
import dagger.hilt.components.SingletonComponent
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.importexport.repository.AutoBackupErrorRepositoryImpl
import studio.lunabee.importexport.repository.AutoBackupSettingsRepositoryImpl
import studio.lunabee.importexport.repository.ImportExportItemRepositoryImpl
import studio.lunabee.importexport.repository.LocalBackupRepositoryImpl
import studio.lunabee.messaging.domain.repository.MessagingSettingsRepository
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.PasswordGeneratorConfigRepository
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.ImportExportItemRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.repository.repository.AutoLockRepositoryImpl
import studio.lunabee.onesafe.repository.repository.BubblesSafeRepositoryImpl
import studio.lunabee.onesafe.repository.repository.ClipboardRepositoryImpl
import studio.lunabee.onesafe.repository.repository.FileRepositoryImpl
import studio.lunabee.onesafe.repository.repository.ForceUpgradeRepositoryImpl
import studio.lunabee.onesafe.repository.repository.IconRepositoryImpl
import studio.lunabee.onesafe.repository.repository.IndexWordEntryRepositoryImpl
import studio.lunabee.onesafe.repository.repository.PasswordGeneratorConfigRepositoryImpl
import studio.lunabee.onesafe.repository.repository.RecentSearchRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemDeletedRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemFieldRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemKeyRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeItemRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SafeRepositoryImpl
import studio.lunabee.onesafe.repository.repository.SettingsRepository
import studio.lunabee.onesafe.repository.repository.SupportOSRepositoryImpl
import studio.lunabee.onesafe.repository.repository.UrlMetadataRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

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
    fun bindUrlMetadataRepository(urlMetadataRepository: UrlMetadataRepositoryImpl): UrlMetadataRepository

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
    fun bindFileRepository(fileRepository: FileRepositoryImpl): FileRepository

    @Binds
    fun bindSupportOSRepository(supportOSRepository: SupportOSRepositoryImpl): SupportOSRepository

    @Binds
    fun bindsBackupRepository(backupRepositoryImpl: LocalBackupRepositoryImpl): LocalBackupRepository

    @Binds
    fun bindsAutoBackupSettingsRepository(autoBackupSettingsRepository: AutoBackupSettingsRepositoryImpl): AutoBackupSettingsRepository

    @Binds
    fun bindsAutoBackupErrorRepository(autoBackupErrorRepository: AutoBackupErrorRepositoryImpl): AutoBackupErrorRepository

    @Binds
    fun bindsImportExportItemRepository(importExportItemRepository: ImportExportItemRepositoryImpl): ImportExportItemRepository

    @Binds
    fun bindsItemSettingsRepository(settingsRepository: SettingsRepository): ItemSettingsRepository

    @Binds
    fun bindsSafeRepository(safeRepository: SafeRepositoryImpl): SafeRepository

    @Binds
    fun bindsAppSettingsRepository(settingsRepository: SettingsRepository): SafeSettingsRepository

    @Binds
    fun bindsAppVisitRepository(settingsRepository: SettingsRepository): AppVisitRepository

    @Binds
    fun bindsMessagingSettingsRepository(settingsRepository: SettingsRepository): MessagingSettingsRepository

    @Binds
    fun bindsBubblesSafeRepository(bubblesSafeRepositoryImpl: BubblesSafeRepositoryImpl): BubblesSafeRepository
}

@Module
@InstallIn(SingletonComponent::class)
interface SecurityOptionModule {
    @Binds
    fun bindSecuritySettingsRepository(settingsRepository: SettingsRepository): SecuritySettingsRepository
}
