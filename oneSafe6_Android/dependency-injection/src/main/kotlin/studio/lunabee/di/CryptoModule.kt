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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import studio.lunabee.bubbles.domain.crypto.BubblesDataHashEngine
import studio.lunabee.bubbles.domain.crypto.BubblesKeyExchangeEngine
import studio.lunabee.bubbles.domain.crypto.BubblesRandomKeyProvider
import studio.lunabee.bubbles.repository.BubblesMainCryptoRepository
import studio.lunabee.onesafe.bubbles.crypto.DiffieHellmanKeyExchangeEngine
import studio.lunabee.onesafe.bubbles.crypto.HKDFHashEngine
import studio.lunabee.onesafe.cryptography.android.AndroidEditCryptoRepository
import studio.lunabee.onesafe.cryptography.android.AndroidImportExportCryptoRepository
import studio.lunabee.onesafe.cryptography.android.AndroidMainCryptoRepository
import studio.lunabee.onesafe.cryptography.android.AndroidMigrationCryptoRepository
import studio.lunabee.onesafe.cryptography.android.AndroidWorkerCryptoRepository
import studio.lunabee.onesafe.cryptography.android.BiometricEngine
import studio.lunabee.onesafe.cryptography.android.BubblesRandomKeyProviderImpl
import studio.lunabee.onesafe.cryptography.android.CryptoConstants
import studio.lunabee.onesafe.cryptography.android.DatabaseKeyRepositoryImpl
import studio.lunabee.onesafe.cryptography.android.DatastoreEngine
import studio.lunabee.onesafe.cryptography.android.EncryptedDataStoreEngine
import studio.lunabee.onesafe.cryptography.android.IVProvider
import studio.lunabee.onesafe.cryptography.android.JceRsaCryptoEngine
import studio.lunabee.onesafe.cryptography.android.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.android.PlainDatastoreEngine
import studio.lunabee.onesafe.cryptography.android.ProtoData
import studio.lunabee.onesafe.cryptography.android.RsaCryptoEngine
import studio.lunabee.onesafe.cryptography.android.SecureIVProvider
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.android.utils.ProtoDataSerializer
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.BiometricCipherRepository
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.domain.repository.WorkerCryptoRepository
import studio.lunabee.onesafe.importexport.repository.ImportExportCryptoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoModule {

    @Binds
    internal abstract fun bindMainCryptoRepository(androidMainCryptoRepository: AndroidMainCryptoRepository): MainCryptoRepository

    @Binds
    internal abstract fun bindBubblesMainCryptoRepository(
        androidMainCryptoRepository: AndroidMainCryptoRepository,
    ): BubblesMainCryptoRepository

    @Binds
    internal abstract fun bindBiometricCipherRepository(biometricEngine: BiometricEngine): BiometricCipherRepository

    @Binds
    internal abstract fun bindEditCryptoRepository(
        androidEditCryptoRepository: AndroidEditCryptoRepository,
    ): EditCryptoRepository

    @Binds
    internal abstract fun bindDatabaseKeyRepository(
        databaseKeyRepository: DatabaseKeyRepositoryImpl,
    ): DatabaseKeyRepository

    @Binds
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal abstract fun bindPlainDatastoreEngine(datastore: PlainDatastoreEngine): DatastoreEngine

    @Binds
    @DatastoreEngineProvider(DataStoreType.Encrypted)
    internal abstract fun bindEncryptedDatastoreEngine(datastore: EncryptedDataStoreEngine): DatastoreEngine

    @Binds
    @Singleton
    internal abstract fun bindImportExportCryptoRepository(
        androidImportExportCryptoRepository: AndroidImportExportCryptoRepository,
    ): ImportExportCryptoRepository

    @Binds
    internal abstract fun bindIVProvider(ivProvider: SecureIVProvider): IVProvider

    @Binds
    internal abstract fun rsaCryptoEngine(rsaCryptoEngine: JceRsaCryptoEngine): RsaCryptoEngine

    @Binds
    internal abstract fun bindDataHashEngine(hkdfHashEngine: HKDFHashEngine): BubblesDataHashEngine

    @Binds
    internal abstract fun bindKeyExchangeEngine(diffieHellmanKeyExchangeEngine: DiffieHellmanKeyExchangeEngine): BubblesKeyExchangeEngine

    @Binds
    internal abstract fun bindsBubblesRandomKeyProvider(
        bubblesRandomKeyProvider: BubblesRandomKeyProviderImpl,
    ): BubblesRandomKeyProvider

    @Binds
    internal abstract fun bindWorkerCryptoRepository(
        androidWorkerCryptoRepository: AndroidWorkerCryptoRepository,
    ): WorkerCryptoRepository
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoDispatcherModule {

    @OptIn(ExperimentalCoroutinesApi::class)
    @CryptoDispatcher
    @Provides
    fun provideCryptoDispatcher(): CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoDatastoreModule {

    private const val ProtoDataDatastoreFile: String = "bad0bd70-277d-4362-86a1-4bcabaa3f1eb"

    @Provides
    fun provideDatastore(
        @ApplicationContext context: Context,
        renameLegacyDatastore: RenameLegacyDatastore,
    ): DataStore<ProtoData> {
        renameLegacyDatastore("e6c59819-f0ea-4e32-bb0f-442e546bf9bd.pb", ProtoDataDatastoreFile)
        return context.dataStoreProto
    }

    private val Context.dataStoreProto: DataStore<ProtoData> by dataStore(
        fileName = ProtoDataDatastoreFile,
        serializer = ProtoDataSerializer,
    )
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoConstantsModule {

    @Provides
    fun provideHashEngineSession(
        @CryptoDispatcher coroutineDispatcher: CoroutineDispatcher,
    ): PasswordHashEngine = PBKDF2JceHashEngine(
        coroutineDispatcher,
        CryptoConstants.PBKDF2Iterations,
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MigrationCryptoModule {
    @Binds
    internal abstract fun bindMigrationCryptoRepository(androidMigrationCryptoRepository: AndroidMigrationCryptoRepository):
        MigrationCryptoRepository
}
