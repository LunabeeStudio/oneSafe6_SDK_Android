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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import studio.lunabee.bubbles.repository.datasource.ContactKeyLocalDataSource
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.importexport.datasource.AutoBackupErrorLocalDataSource
import studio.lunabee.importexport.datasource.AutoBackupSettingsDataSource
import studio.lunabee.importexport.datasource.CloudBackupLocalDataSource
import studio.lunabee.importexport.datasource.ImportExportBubblesLocalDatasource
import studio.lunabee.importexport.datasource.ImportExportSafeItemLocalDataSource
import studio.lunabee.importexport.datasource.LocalBackupCacheDataSource
import studio.lunabee.importexport.datasource.LocalBackupLocalDataSource
import studio.lunabee.messaging.repository.datasource.ConversationLocalDatasource
import studio.lunabee.messaging.repository.datasource.DoubleRatchetKeyLocalDatasource
import studio.lunabee.messaging.repository.datasource.EnqueuedMessageLocalDataSource
import studio.lunabee.messaging.repository.datasource.HandShakeDataLocalDatasource
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.messaging.repository.datasource.MessagePagingLocalDataSource
import studio.lunabee.messaging.repository.datasource.MessageQueueLocalDatasource
import studio.lunabee.messaging.repository.datasource.SentMessageLocalDatasource
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.StorageManager
import studio.lunabee.onesafe.importexport.data.GoogleDriveEnginePreferencesDatasource
import studio.lunabee.onesafe.repository.datasource.FileLocalDatasource
import studio.lunabee.onesafe.repository.datasource.ForceUpgradeLocalDatasource
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import studio.lunabee.onesafe.repository.datasource.IndexWordEntryLocalDataSource
import studio.lunabee.onesafe.repository.datasource.PasswordGeneratorConfigLocalDataSource
import studio.lunabee.onesafe.repository.datasource.RecentSearchLocalDatasource
import studio.lunabee.onesafe.repository.datasource.SafeIdCacheDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemFieldLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemKeyLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeLocalDataSource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.MainDatabaseTransactionManager
import studio.lunabee.onesafe.storage.OSForceUpgradeProto.ForceUpgradeProtoData
import studio.lunabee.onesafe.storage.OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto
import studio.lunabee.onesafe.storage.OSRecentSearchProto.RecentSearchProto
import studio.lunabee.onesafe.storage.SqlCipherDBManager
import studio.lunabee.onesafe.storage.dao.AutoBackupErrorDao
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetConversationDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetKeyDao
import studio.lunabee.onesafe.storage.dao.EnqueuedMessageDao
import studio.lunabee.onesafe.storage.dao.HandShakeDataDao
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.dao.SafeDao
import studio.lunabee.onesafe.storage.dao.SafeFileDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.dao.SafeItemRawDao
import studio.lunabee.onesafe.storage.dao.SentMessageDao
import studio.lunabee.onesafe.storage.dao.SettingsDao
import studio.lunabee.onesafe.storage.datasource.AutoBackupErrorLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.CloudBackupLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ContactKeyLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ContactLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ConversationLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.DoubleRatchetKeyLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.EnqueuedMessageLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.FileLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.ForceUpgradeLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.HandShakeDataLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.IconLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ImportExportBubblesLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.IndexWordEntryLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.LocalBackupFileCacheDataSource
import studio.lunabee.onesafe.storage.datasource.LocalBackupLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.MessageLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.MessageQueueLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.PasswordGeneratorConfigLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.RecentSearchLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeIdMemoryDataSource
import studio.lunabee.onesafe.storage.datasource.SafeItemFieldLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeItemKeyLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeItemLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SentMessageLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.SettingsLocalDataSource
import studio.lunabee.onesafe.storage.datastore.ForceUpgradeDataSerializer
import studio.lunabee.onesafe.storage.datastore.PasswordGeneratorConfigSerializer
import studio.lunabee.onesafe.storage.datastore.RecentSearchSerializer
import studio.lunabee.onesafe.storage.migration.RoomMigration12to13
import studio.lunabee.onesafe.storage.migration.RoomMigration13to14
import studio.lunabee.onesafe.storage.migration.RoomMigration15to16
import studio.lunabee.onesafe.storage.migration.RoomMigration16to17
import studio.lunabee.onesafe.storage.migration.RoomMigration3to4
import studio.lunabee.onesafe.storage.migration.RoomMigration8to9
import studio.lunabee.onesafe.storage.migration.RoomMigration9to10
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface StorageModule {
    @Binds
    fun bindSafeItemLocalDataSource(safeItemLocalDataSourceImpl: SafeItemLocalDataSourceImpl): SafeItemLocalDataSource

    @Binds
    fun bindSafeItemKeyLocalDataSource(safeItemKeyLocalDataSourceImpl: SafeItemKeyLocalDataSourceImpl): SafeItemKeyLocalDataSource

    @Binds
    fun bindSafeItemFieldLocalDataSource(
        safeItemFieldLocalDataSourceImpl: SafeItemFieldLocalDataSourceImpl,
    ): SafeItemFieldLocalDataSource

    @Binds
    fun bindIconLocalDataSource(iconLocalDataSourceImpl: IconLocalDataSourceImpl): IconLocalDataSource

    @Binds
    fun bindFileLocalDataSource(fileLocalDatasource: FileLocalDatasourceImpl): FileLocalDatasource

    @Binds
    fun bindIndexWordEntryLocalDataSource(
        indexWordEntryLocalDataSourceImpl: IndexWordEntryLocalDataSourceImpl,
    ): IndexWordEntryLocalDataSource

    @Binds
    fun bindImportExportBubblesLocalDataSource(
        importExportBubblesLocalDatasourceImpl: ImportExportBubblesLocalDatasourceImpl,
    ): ImportExportBubblesLocalDatasource

    @Binds
    fun bindForceUpgradeLocalDatasource(
        forceUpgradeLocalDatasourceImpl: ForceUpgradeLocalDatasourceImpl,
    ): ForceUpgradeLocalDatasource

    @Binds
    fun bindRecentSearchLocalDatasource(
        recentSearchLocalDataSourceImpl: RecentSearchLocalDataSourceImpl,
    ): RecentSearchLocalDatasource

    @Binds
    fun bindPasswordGeneratorConfigLocalDatasource(
        passwordGeneratorConfigLocalDataSourceImpl: PasswordGeneratorConfigLocalDataSourceImpl,
    ): PasswordGeneratorConfigLocalDataSource

    @Binds
    fun bindContactLocalDataSource(
        bindContactLocalDataSourceImpl: ContactLocalDataSourceImpl,
    ): ContactLocalDataSource

    @Binds
    fun bindContactKeyLocalDataSource(
        bindContactKeyLocalDataSourceImpl: ContactKeyLocalDataSourceImpl,
    ): ContactKeyLocalDataSource

    @Binds
    fun bindMessageLocalDataSource(
        bindBubblesMessageLocalDataSourceImpl: MessageLocalDataSourceImpl,
    ): MessageLocalDataSource

    @Binds
    fun bindMessagePagingLocalDataSource(
        bindBubblesMessagePagingLocalDataSourceImpl: MessageLocalDataSourceImpl,
    ): MessagePagingLocalDataSource

    @Binds
    fun bindSentMessageLocalDataSource(
        bindSentBubblesMessageLocalDataSourceImpl: SentMessageLocalDatasourceImpl,
    ): SentMessageLocalDatasource

    @Binds
    fun bindEnqueuedMessageLocalDataSource(
        enqueuedMessageLocalDataSourceImpl: EnqueuedMessageLocalDataSourceImpl,
    ): EnqueuedMessageLocalDataSource

    @Binds
    fun bindBackupLocalDataSource(
        backupMessageLocalDataSourceImpl: LocalBackupLocalDataSourceImpl,
    ): LocalBackupLocalDataSource

    @Binds
    fun bindLocalBackupCacheDataSource(
        backupMessageLocalDataSourceImpl: LocalBackupFileCacheDataSource,
    ): LocalBackupCacheDataSource

    @Binds
    fun bindCloudBackupLocalDataSource(
        cloudBackupLocalDataSourceImpl: CloudBackupLocalDataSourceImpl,
    ): CloudBackupLocalDataSource

    @Binds
    fun bindsHandShakeDataLocalDatasource(handShakeDataLocalDatasourceImpl: HandShakeDataLocalDatasourceImpl): HandShakeDataLocalDatasource

    @Binds
    fun bindsConversationLocalDatasource(conversationLocalDatasourceImpl: ConversationLocalDatasourceImpl): ConversationLocalDatasource

    @Binds
    fun bindsDoubleRatchetKeyLocalDatasource(
        doubleRatchetKeyLocalDatasourceImpl: DoubleRatchetKeyLocalDatasourceImpl,
    ): DoubleRatchetKeyLocalDatasource

    @Binds
    fun bindAutoBackupErrorDataSource(
        autoBackupErrorDataSource: AutoBackupErrorLocalDataSourceImpl,
    ): AutoBackupErrorLocalDataSource

    @Binds
    fun bindImportExportSafeItemLocalDataSource(
        importExportSafeItemLocalDataSource: SafeItemLocalDataSourceImpl,
    ): ImportExportSafeItemLocalDataSource

    @Binds
    fun bindSafeLocalDataSource(
        safeDatabaseDataSource: SafeLocalDataSourceImpl,
    ): SafeLocalDataSource

    @Binds
    fun bindSafeCacheDataSource(
        safeMemoryDataSource: SafeIdMemoryDataSource,
    ): SafeIdCacheDataSource

    @Binds
    fun bindAutoBackupSettingsDataSource(
        settingsLocalDataSource: SettingsLocalDataSource,
    ): AutoBackupSettingsDataSource

    @Binds
    fun bindGoogleDriveEnginePreferencesDatasource(
        settingsLocalDataSource: SettingsLocalDataSource,
    ): GoogleDriveEnginePreferencesDatasource

    @Binds
    fun bindsMessageQueueLocalDatasource(
        messageQueueLocalDatasourceImpl: MessageQueueLocalDatasourceImpl,
    ): MessageQueueLocalDatasource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @DatabaseName(DatabaseName.Type.Main)
    fun provideMainDatabaseName(): String = "bc9fe798-a4f0-402e-9f5b-80339d87a041"

    @Provides
    @DatabaseName(DatabaseName.Type.CipherTemp)
    fun provideCipherTempDatabaseName(): String = "c0308558-69cc-49ee-a096-fbbf0da408c1"

    @Provides
    @Singleton
    @Suppress("LongParameterList")
    fun provideMainDatabase(
        @ApplicationContext appContext: Context,
        migration3to4: RoomMigration3to4,
        migration8to9: RoomMigration8to9,
        migration9to10: RoomMigration9to10,
        migration12to13: RoomMigration12to13,
        migration13to14: RoomMigration13to14,
        migration15to16: RoomMigration15to16,
        migration16to17: RoomMigration16to17,
        databaseKeyRepository: DatabaseKeyRepository,
        @DatabaseName(DatabaseName.Type.Main) dbName: String,
    ): MainDatabase {
        return runBlocking {
            val dbKey = kotlin.runCatching { databaseKeyRepository.getKeyFlow().firstOrNull() }.getOrNull()
            MainDatabase.build(
                appContext = appContext,
                dbKey = dbKey,
                mainDatabaseName = dbName,
                migration3to4,
                migration8to9,
                migration9to10,
                migration12to13,
                migration13to14,
                migration15to16,
                migration16to17,
            )
        }
    }

    @Provides
    fun provideSqlCipherManager(
        @FileDispatcher dispatcher: CoroutineDispatcher,
        @ApplicationContext context: Context,
        @DatabaseName(DatabaseName.Type.Main) dbName: String,
        @DatabaseName(DatabaseName.Type.CipherTemp) tempDbName: String,
    ): DatabaseEncryptionManager {
        return SqlCipherDBManager(dispatcher, context, dbName, tempDbName)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MainDatabaseModule {
    @Provides
    fun provideTransactionManager(
        mainDatabase: MainDatabase,
        @ApplicationContext context: Context,
        @FileDispatcher dispatcher: CoroutineDispatcher,
    ): StorageManager {
        return MainDatabaseTransactionManager(
            mainDatabase = mainDatabase,
            context = context,
            dispatcher = dispatcher,
        )
    }

    @Provides
    fun provideSafeItemDao(mainDatabase: MainDatabase): SafeItemDao {
        return mainDatabase.safeItemDao()
    }

    @Provides
    fun provideSafeItemRawDao(mainDatabase: MainDatabase): SafeItemRawDao {
        return mainDatabase.safeItemRawDao()
    }

    @Provides
    fun provideSafeItemFieldDao(mainDatabase: MainDatabase): SafeItemFieldDao {
        return mainDatabase.safeItemFieldDao()
    }

    @Provides
    fun provideSearchIndexDao(mainDatabase: MainDatabase): IndexWordEntryDao {
        return mainDatabase.searchIndexDao()
    }

    @Provides
    fun provideSafeItemKeyDao(mainDatabase: MainDatabase): SafeItemKeyDao {
        return mainDatabase.safeItemKeyDao()
    }

    @Provides
    fun provideContactDao(mainDatabase: MainDatabase): ContactDao {
        return mainDatabase.contactDao()
    }

    @Provides
    fun provideContactKeyDao(mainDatabase: MainDatabase): ContactKeyDao {
        return mainDatabase.contactKeyDao()
    }

    @Provides
    fun provideMessageDao(mainDatabase: MainDatabase): MessageDao {
        return mainDatabase.messageDao()
    }

    @Provides
    fun provideEnqueuedMessageDao(mainDatabase: MainDatabase): EnqueuedMessageDao {
        return mainDatabase.enqueuedMessageDao()
    }

    @Provides
    fun provideMessageKeyDao(mainDatabase: MainDatabase): DoubleRatchetKeyDao {
        return mainDatabase.doubleRatchetKeyDao()
    }

    @Provides
    fun provideConversationDao(mainDatabase: MainDatabase): DoubleRatchetConversationDao {
        return mainDatabase.doubleRatchetConversationDao()
    }

    @Provides
    fun provideHandShakeDataDao(mainDatabase: MainDatabase): HandShakeDataDao {
        return mainDatabase.handShakeDataDao()
    }

    @Provides
    fun provideSentMessageDao(mainDatabase: MainDatabase): SentMessageDao {
        return mainDatabase.sentMessageDao()
    }

    @Provides
    fun provideBackupDao(mainDatabase: MainDatabase): BackupDao {
        return mainDatabase.backupDao()
    }

    @Provides
    fun provideSafeDao(mainDatabase: MainDatabase): SafeDao {
        return mainDatabase.safeDao()
    }

    @Provides
    fun provideSettingsDao(mainDatabase: MainDatabase): SettingsDao {
        return mainDatabase.settingsDao()
    }

    @Provides
    fun provideAutoBackupErrorDao(mainDatabase: MainDatabase): AutoBackupErrorDao {
        return mainDatabase.autoBackupErrorDao()
    }

    @Provides
    fun provideSafeFileDao(mainDatabase: MainDatabase): SafeFileDao {
        return mainDatabase.safeFileDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppMaintenanceDatastoreModule {

    private const val datastoreFile: String = "e6c59819-f0ea-4e32-bb0f-442e546bf9bd"

    @Provides
    @Singleton
    fun provideDatastore(@ApplicationContext context: Context): DataStore<ForceUpgradeProtoData> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<ForceUpgradeProtoData> by dataStore(
        fileName = datastoreFile,
        serializer = ForceUpgradeDataSerializer,
    )
}

@Module
@InstallIn(SingletonComponent::class)
object RecentSearchDatastoreModule {

    private const val datastoreFile: String = "201c4654-39bd-400e-a3ef-3408e0729273"

    @Provides
    @Singleton
    fun provideDatastore(@ApplicationContext context: Context): DataStore<RecentSearchProto> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<RecentSearchProto> by dataStore(
        fileName = datastoreFile,
        serializer = RecentSearchSerializer,
    )
}

@Module
@InstallIn(SingletonComponent::class)
object PasswordGeneratorConfigDatastoreModule {

    private const val datastoreFile: String = "5a483983-a9c3-4385-8747-7baff8d6b29b"

    @Provides
    @Singleton
    fun provideDatastore(@ApplicationContext context: Context): DataStore<PasswordGeneratorConfigProto> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<PasswordGeneratorConfigProto> by dataStore(
        fileName = datastoreFile,
        serializer = PasswordGeneratorConfigSerializer,
    )
}
