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
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import studio.lunabee.onesafe.bubbles.crypto.AndroidBubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.cryptography.AndroidEditCryptoRepository
import studio.lunabee.onesafe.cryptography.AndroidImportExportCryptoRepository
import studio.lunabee.onesafe.cryptography.AndroidMainCryptoRepository
import studio.lunabee.onesafe.cryptography.AndroidMigrationCryptoRepository
import studio.lunabee.onesafe.cryptography.ClearDatastoreEngine
import studio.lunabee.onesafe.cryptography.CryptoConstants
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.EncryptedDataStoreEngine
import studio.lunabee.onesafe.cryptography.HashEngine
import studio.lunabee.onesafe.cryptography.IVProvider
import studio.lunabee.onesafe.cryptography.JceRsaCryptoEngine
import studio.lunabee.onesafe.cryptography.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.ProtoData
import studio.lunabee.onesafe.cryptography.RsaCryptoEngine
import studio.lunabee.onesafe.cryptography.SecureIVProvider
import studio.lunabee.onesafe.cryptography.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.qualifier.PBKDF2Iterations
import studio.lunabee.onesafe.cryptography.utils.SecuredDataSerializer
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.importexport.ImportExportCryptoRepository
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class CryptoModule {

    @Binds
    @ActivityRetainedScoped
    internal abstract fun bindMainCryptoRepository(androidMainCryptoRepository: AndroidMainCryptoRepository): MainCryptoRepository

    @Binds
    @ActivityRetainedScoped
    internal abstract fun bindEditCryptoRepository(
        androidEditCryptoRepository: AndroidEditCryptoRepository,
    ): EditCryptoRepository

    @Binds
    @ActivityRetainedScoped
    internal abstract fun bindBubblesCryptoRepository(
        androidBubblesCryptoRepository: AndroidBubblesCryptoRepository,
    ): BubblesCryptoRepository
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class CryptoMigrationModule {

    @Binds
    @ActivityRetainedScoped
    internal abstract fun bindMigrationCryptoRepository(androidMigrationCryptoRepository: AndroidMigrationCryptoRepository):
        MigrationCryptoRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoDispatcherModule {

    @CryptoDispatcher
    @Provides
    fun provideCryptoDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoDatastoreModule {

    private const val EncProtoDatastoreFile: String = "e6c59819-f0ea-4e32-bb0f-442e546bf9bd.pb"

    @Provides
    fun provideDatastore(@ApplicationContext context: Context): DataStore<ProtoData> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<ProtoData> by dataStore(
        fileName = EncProtoDatastoreFile,
        serializer = SecuredDataSerializer,
    )
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class DatastoreEngineModule {
    @Binds
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal abstract fun bindClearDatastoreEngine(datastore: ClearDatastoreEngine): DatastoreEngine

    @Binds
    @DatastoreEngineProvider(DataStoreType.Encrypted)
    internal abstract fun bindEncryptedDatastoreEngine(datastore: EncryptedDataStoreEngine): DatastoreEngine
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoConstantsModule {
    @Provides
    @PBKDF2Iterations
    fun providePBKDF2Iterations(): Int = CryptoConstants.PBKDF2Iterations
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GlobalCryptoModule {
    @Binds
    @Singleton
    internal abstract fun bindImportExportCryptoRepository(androidImportExportCryptoRepository: AndroidImportExportCryptoRepository):
        ImportExportCryptoRepository

    @Binds
    internal abstract fun bindIVProvider(ivProvider: SecureIVProvider): IVProvider

    @Binds
    internal abstract fun bindHashEngine(hashEngine: PBKDF2JceHashEngine): HashEngine

    @Binds
    internal abstract fun rsaCryptoEngine(rsaCryptoEngine: JceRsaCryptoEngine): RsaCryptoEngine
}

@Module
@InstallIn(ServiceComponent::class)
abstract class CryptoServiceModule {

    @Binds
    @ServiceScoped
    internal abstract fun bindMainCryptoRepository(androidMainCryptoRepository: AndroidMainCryptoRepository): MainCryptoRepository

    @Binds
    internal abstract fun bindEditCryptoRepository(
        androidEditCryptoRepository: AndroidEditCryptoRepository,
    ): EditCryptoRepository

    @Binds
    @ServiceScoped
    internal abstract fun bindBubblesCryptoRepository(
        androidBubblesCryptoRepository: AndroidBubblesCryptoRepository,
    ): BubblesCryptoRepository
}

@Module
@InstallIn(ServiceComponent::class)
abstract class DatastoreEngineServiceModule {
    @Binds
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal abstract fun bindClearDatastoreEngine(datastore: ClearDatastoreEngine): DatastoreEngine

    @Binds
    @DatastoreEngineProvider(DataStoreType.Encrypted)
    internal abstract fun bindEncryptedDatastoreEngine(datastore: EncryptedDataStoreEngine): DatastoreEngine
}

@Module
@InstallIn(ServiceComponent::class)
abstract class CryptoMigrationServiceModule {

    @Binds
    internal abstract fun bindMigrationCryptoRepository(androidMigrationCryptoRepository: AndroidMigrationCryptoRepository):
        MigrationCryptoRepository
}
