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
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.domain.repository.PersistenceManager
import studio.lunabee.onesafe.repository.datasource.ForceUpgradeLocalDatasource
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import studio.lunabee.onesafe.repository.datasource.IndexWordEntryLocalDataSource
import studio.lunabee.onesafe.repository.datasource.PasswordGeneratorConfigLocalDataSource
import studio.lunabee.onesafe.repository.datasource.RecentSearchLocalDatasource
import studio.lunabee.onesafe.repository.datasource.SafeItemFieldLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemKeyLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeItemLocalDataSource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.OSForceUpgradeProto.ForceUpgradeProtoData
import studio.lunabee.onesafe.storage.OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto
import studio.lunabee.onesafe.storage.OSRecentSearchProto.RecentSearchProto
import studio.lunabee.onesafe.storage.PersistenceManagerImpl
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.datasource.ForceUpgradeLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.IconLocalDataSourceImpl
import studio.lunabee.onesafe.storage.datasource.IndexWordEntryLocalDataSourceImpl
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
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [StorageModule::class],
)
interface StorageTestModule {
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
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PersistenceManagerModule::class],
)
object PersistenceManagerTestModule {
    @Provides
    fun providePersistenceManager(
        mainDatabase: MainDatabase,
        iconLocalDataSource: IconLocalDataSource,
        recentSearchLocalDatasource: RecentSearchLocalDatasource,
    ): PersistenceManager {
        return PersistenceManagerImpl(mainDatabase, iconLocalDataSource, recentSearchLocalDatasource)
    }
}

// Use empty TestInstallIn + InstallIn to allow local override of the module (in SearchItemUseCaseTest for example)
// See the note in @UninstallModules section of https://dagger.dev/hilt/testing.html

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [MainDatabaseModule::class],
)
object EmptyMainDatabaseModule

@Module
@InstallIn(SingletonComponent::class)
object InMemoryMainDatabaseModule {

    @Provides
    @Singleton
    internal fun provideMainDatabase(@ApplicationContext appContext: Context): MainDatabase {
        return Room.inMemoryDatabaseBuilder(appContext, MainDatabase::class.java)
            .build()
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [MainDatabaseDaoModule::class],
)
object MainDatabaseDaoTestModule {
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
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppMaintenanceDatastoreModule::class],
)
object AppMaintenanceDatastoreTestModule {

    private const val datastoreFile: String = "force_upgrade_datastore"

    @Provides
    fun provideDatastore(@ApplicationContext context: Context): DataStore<ForceUpgradeProtoData> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<ForceUpgradeProtoData> by dataStore(
        fileName = datastoreFile,
        serializer = ForceUpgradeDataSerializer,
    )
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RecentSearchDatastoreModule::class],
)
object RecentSearchDatastoreTestModule {

    private const val datastoreFile: String = "recent_search_datastore"

    @Provides
    fun provideDatastore(@ApplicationContext context: Context): DataStore<RecentSearchProto> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<RecentSearchProto> by dataStore(
        fileName = datastoreFile,
        serializer = RecentSearchSerializer,
    )
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PasswordGeneratorConfigDatastoreModule::class],
)
object PasswordGeneratorConfigDatastoreTestModule {

    private const val datastoreFile: String = "password_config_datastore"

    @Provides
    fun provideDatastore(@ApplicationContext context: Context): DataStore<PasswordGeneratorConfigProto> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<PasswordGeneratorConfigProto> by dataStore(
        fileName = datastoreFile,
        serializer = PasswordGeneratorConfigSerializer,
    )
}
