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
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.lunabee.bubbles.repository.datasource.ContactKeyLocalDataSource
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.onesafe.domain.repository.PersistenceManager
import studio.lunabee.onesafe.repository.datasource.ForceUpgradeLocalDatasource
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import studio.lunabee.onesafe.repository.datasource.IndexWordEntryLocalDataSource
import studio.lunabee.onesafe.repository.datasource.PasswordGeneratorConfigLocalDataSource
import studio.lunabee.onesafe.repository.datasource.RecentSearchLocalDatasource
import studio.lunabee.onesafe.repository.datasource.SafeItemFieldLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemKeyLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import studio.lunabee.onesafe.storage.BubblesDatabase
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.OSForceUpgradeProto.ForceUpgradeProtoData
import studio.lunabee.onesafe.storage.OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto
import studio.lunabee.onesafe.storage.OSRecentSearchProto.RecentSearchProto
import studio.lunabee.onesafe.storage.PersistenceManagerImpl
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.datasource.ContactKeyLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ContactLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.ForceUpgradeLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.IconLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.IndexWordEntryLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.MessageLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.PasswordGeneratorConfigLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.RecentSearchLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeItemFieldLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeItemKeyLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.SafeItemLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datastore.ForceUpgradeDataSerializer
import studio.lunabee.onesafe.storage.datastore.PasswordGeneratorConfigSerializer
import studio.lunabee.onesafe.storage.datastore.RecentSearchSerializer
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
    fun bindIndexWordEntryLocalDataSource(
        indexWordEntryLocalDataSourceImpl: IndexWordEntryLocalDataSourceImpl,
    ): IndexWordEntryLocalDataSource

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
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMainDatabase(@ApplicationContext appContext: Context): MainDatabase {
        return Room.databaseBuilder(
            appContext,
            MainDatabase::class.java,
            "bc9fe798-a4f0-402e-9f5b-80339d87a041",
        ).build()
    }

    @Provides
    @Singleton
    fun provideBubblesDatabase(@ApplicationContext appContext: Context): BubblesDatabase {
        return Room.databaseBuilder(
            appContext,
            BubblesDatabase::class.java,
            "58252457-42de-4313-a007-1b7c80445bf6",
        ).build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MainDatabaseDaoModule {
    @Provides
    fun provideSafeItemDao(mainDatabase: MainDatabase): SafeItemDao {
        return mainDatabase.safeItemDao()
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
    fun provideContactDao(bubblesDatabase: BubblesDatabase): ContactDao {
        return bubblesDatabase.contactDao()
    }

    @Provides
    fun provideContactKeyDao(bubblesDatabase: BubblesDatabase): ContactKeyDao {
        return bubblesDatabase.contactKeyDao()
    }

    @Provides
    fun provideMessageDao(bubblesDatabase: BubblesDatabase): MessageDao {
        return bubblesDatabase.messageDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PersistenceManagerModule {
    @Provides
    fun providePersistenceManager(
        mainDatabase: MainDatabase,
        iconLocalDataSource: IconLocalDataSource,
        recentSearchLocalDatasource: RecentSearchLocalDatasource,
    ): PersistenceManager {
        return PersistenceManagerImpl(mainDatabase, iconLocalDataSource, recentSearchLocalDatasource)
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
