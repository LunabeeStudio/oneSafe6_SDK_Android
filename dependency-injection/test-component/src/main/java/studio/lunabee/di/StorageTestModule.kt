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
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.messaging.repository.datasource.HandShakeDataLocalDatasource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.OSForceUpgradeProto.ForceUpgradeProtoData
import studio.lunabee.onesafe.storage.datasource.DoubleRatchetDatasourceImpl
import studio.lunabee.onesafe.storage.datasource.HandShakeDataLocalDatasourceImpl
import studio.lunabee.onesafe.storage.datastore.ForceUpgradeDataSerializer
import javax.inject.Singleton

// Use empty TestInstallIn + InstallIn to allow local override of the module (in SearchItemUseCaseTest for example)
// See the note in @UninstallModules section of https://dagger.dev/hilt/testing.html

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class],
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
    replaces = [DoubleRatchetActivityStorageModule::class, DoubleRatchetServiceStorageModule::class],
)
interface DoubleRatchetTestStorageModule {
    @Binds
    fun bindDoubleRatchetDatasource(
        doubleRatchetDatasource: DoubleRatchetDatasourceImpl,
    ): DoubleRatchetLocalDatasource

    @Binds
    fun bindsHandShakeDataLocalDatasource(handShakeDataLocalDatasourceImpl: HandShakeDataLocalDatasourceImpl): HandShakeDataLocalDatasource
}
