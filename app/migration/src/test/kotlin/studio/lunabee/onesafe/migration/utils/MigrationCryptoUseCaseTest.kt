/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/23/2024 - for the oneSafe6 SDK.
 * Last modified 9/23/24, 2:50 PM
 */

package studio.lunabee.onesafe.migration.utils

import androidx.core.util.AtomicFile
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import studio.lunabee.onesafe.cryptography.android.CryptoEngine
import studio.lunabee.onesafe.cryptography.android.DatastoreEngine
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.migration.MigrationConstant
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import java.io.File
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals

@HiltAndroidTest
class MigrationCryptoUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject
    lateinit var migrationCryptoUseCase: MigrationCryptoUseCase

    @Inject
    lateinit var cryptoEngine: CryptoEngine

    @Inject
    @DatastoreEngineProvider(DataStoreType.Plain)
    lateinit var dataStoreEngine: DatastoreEngine

    private val cipherFile = File("cipher_temp")

    @After
    fun tearsDown() {
        cipherFile.delete()
    }

    /**
     * Test encrypt & getCipherOutputStream with version code 0
     */
    @Test
    fun encrypt_v0_test(): TestResult = runTest {
        val key = OSTestConfig.random.nextBytes(32)
        val plainData = OSTestConfig.random.nextBytes(128)
        val ad = OSTestConfig.random.nextBytes(32)
        dataStoreEngine.insertValue(MigrationConstant.DATASTORE_USERNAME_V0, ad)

        val actualCipherData = migrationCryptoUseCase.encrypt(plainData, key, 0)
        migrationCryptoUseCase.getCipherOutputStream(cipherFile.outputStream(), key, 0).use { outputStream ->
            outputStream.write(plainData)
        }

        val actualPlainData = cryptoEngine.decrypt(actualCipherData, key, ad).getOrThrow()
        assertContentEquals(plainData, actualPlainData)

        val actualPlainDataFile = cryptoEngine.decrypt(cipherFile.readBytes(), key, ad).getOrThrow()
        assertContentEquals(plainData, actualPlainDataFile)
    }

    /**
     * Test decrypt & getDecryptStream with version code 0
     */
    @Test
    fun decrypt_v0_test(): TestResult = runTest {
        val key = OSTestConfig.random.nextBytes(32)
        val plainData = OSTestConfig.random.nextBytes(128)
        val ad = OSTestConfig.random.nextBytes(32)
        val cipherData = cryptoEngine.encrypt(plainData, key, ad).getOrThrow()
        dataStoreEngine.insertValue(MigrationConstant.DATASTORE_USERNAME_V0, ad)
        cipherFile.writeBytes(cryptoEngine.encrypt(plainData, key, ad).getOrThrow())

        val actualData = migrationCryptoUseCase.decrypt(cipherData, key, 0)
        assertContentEquals(plainData, actualData)

        val actualDataFile = migrationCryptoUseCase.getDecryptStream(AtomicFile(cipherFile), key, 0).readAllBytes()
        assertContentEquals(plainData, actualDataFile)
    }

    /**
     *  Test encrypt & getCipherOutputStream with version code 1
     */
    @Test
    fun encrypt_v1_test(): TestResult = runTest {
        val key = OSTestConfig.random.nextBytes(32)
        val plainData = OSTestConfig.random.nextBytes(128)

        val actualCipherData = migrationCryptoUseCase.encrypt(plainData, key, 1)
        migrationCryptoUseCase.getCipherOutputStream(cipherFile.outputStream(), key, 1).use { outputStream ->
            outputStream.write(plainData)
        }

        val actualPlainData = cryptoEngine.decrypt(actualCipherData, key, null).getOrThrow()
        assertContentEquals(plainData, actualPlainData)

        val actualPlainDataFile = cryptoEngine.decrypt(cipherFile.readBytes(), key, null).getOrThrow()
        assertContentEquals(plainData, actualPlainDataFile)
    }

    /**
     * Test decrypt & getDecryptStream with version code 1
     */
    @Test
    fun decrypt_v1_test(): TestResult = runTest {
        val key = OSTestConfig.random.nextBytes(32)
        val plainData = OSTestConfig.random.nextBytes(128)
        val cipherData = cryptoEngine.encrypt(plainData, key, null).getOrThrow()
        cipherFile.writeBytes(cryptoEngine.encrypt(plainData, key, null).getOrThrow())

        val actualData = migrationCryptoUseCase.decrypt(cipherData, key, 1)
        assertContentEquals(plainData, actualData)

        val actualDataFile = migrationCryptoUseCase.getDecryptStream(AtomicFile(cipherFile), key, 1).readAllBytes()
        assertContentEquals(plainData, actualDataFile)
    }
}
