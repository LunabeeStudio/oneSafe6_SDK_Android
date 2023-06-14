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
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.cryptography.AndroidEditCryptoRepository
import studio.lunabee.onesafe.cryptography.AndroidMainCryptoRepository
import studio.lunabee.onesafe.cryptography.ClearDatastoreEngine
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.EncryptedDataStoreEngine
import studio.lunabee.onesafe.cryptography.ProtoData
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.qualifier.PBKDF2Iterations
import studio.lunabee.onesafe.cryptography.utils.SecuredDataSerializer
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CryptoModule::class],
)
abstract class CryptoTestModule {
    @Binds
    @Singleton
    internal abstract fun bindMainCryptoRepository(
        cryptoRepository: AndroidMainCryptoRepository,
    ): MainCryptoRepository

    @Binds
    @Singleton
    internal abstract fun bindOnboardingCryptoRepository(
        androidEditCryptoRepository: AndroidEditCryptoRepository,
    ): EditCryptoRepository
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CryptoDatastoreModule::class],
)
object CryptoDatastoreTestModule {

    private const val EncProtoDatastoreFile: String = "crypto_datastore"

    @Provides
    fun provideDatastore(@ApplicationContext context: Context): DataStore<ProtoData> = context.dataStoreProto

    private val Context.dataStoreProto: DataStore<ProtoData> by dataStore(
        fileName = EncProtoDatastoreFile,
        serializer = SecuredDataSerializer,
    )
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatastoreEngineModule::class],
)
abstract class DatastoreEngineTestModule {
    @Binds
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal abstract fun bindClearDatastoreEngine(datastore: ClearDatastoreEngine): DatastoreEngine

    @Binds
    @DatastoreEngineProvider(DataStoreType.Encrypted)
    internal abstract fun bindEncryptedDatastoreEngine(datastore: EncryptedDataStoreEngine): DatastoreEngine
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CryptoConstantsModule::class],
)
object EmptyCryptoConstantModule

@Module
@InstallIn(SingletonComponent::class)
object CryptoConstantsTestModule {
    @Provides
    @PBKDF2Iterations
    @Suppress("FunctionOnlyReturningConstant")
    fun providePBKDF2Iterations(): Int = 1
}
