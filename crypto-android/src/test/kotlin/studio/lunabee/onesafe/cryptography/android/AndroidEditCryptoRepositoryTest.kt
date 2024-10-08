/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/10/2024 - for the oneSafe6 SDK.
 * Last modified 9/10/24, 9:25 AM
 */

package studio.lunabee.onesafe.cryptography.android

import android.security.keystore.KeyProperties
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.assertThrows
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class AndroidEditCryptoRepositoryTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var repository: AndroidEditCryptoRepository

    @Inject
    internal lateinit var mainRepository: AndroidMainCryptoRepository

    @Inject
    internal lateinit var safeRepository: SafeRepository

    @Inject
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal lateinit var dataStoreEngine: DatastoreEngine

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun generate_and_check_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertTrue { repository.checkCryptographicData(password) }
    }

    @Test
    fun generate_and_check_no_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertFalse { repository.checkCryptographicData(charArrayOf('a')) }
    }

    @Test
    fun check_not_generated_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.checkCryptographicData(password) }

        repository.generateCryptographicData(password)
        repository.reset()
        assertThrows<OSCryptoError> { repository.checkCryptographicData(password) }
    }

    @Test
    fun persist_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertDoesNotThrow { repository.setMainCryptographicData() }
    }

    @Test
    fun persist_not_generated_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.setMainCryptographicData() }

        repository.generateCryptographicData(password)
        repository.reset()
        assertThrows<OSCryptoError> { repository.setMainCryptographicData() }
    }

    @Test
    fun initializeBiometric_and_check_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7,
        )
        repository.initializeBiometric(cipher)
        assertTrue { repository.checkCryptographicData(password) }
    }

    @Test
    fun persist_biometric_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7,
        )
        repository.initializeBiometric(cipher)
        assertTrue { repository.checkCryptographicData(password) }
    }

    @Test
    fun reEncryptItemKeys_no_key_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.reEncryptItemKeys(listOf()) }
    }

    companion object {
        private val password: CharArray
            get() = "password".toCharArray()
    }
}
