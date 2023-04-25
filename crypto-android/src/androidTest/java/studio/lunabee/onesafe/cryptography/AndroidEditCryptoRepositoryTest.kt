/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.cryptography

import android.security.keystore.KeyProperties
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.error.OSCryptoError
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class AndroidEditCryptoRepositoryTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var repository: AndroidEditCryptoRepository

    @Inject
    internal lateinit var mainRepository: AndroidMainCryptoRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            mainRepository.resetCryptography()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun generate_and_check_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertTrue { repository.checkCryptographicData(password) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun generate_and_check_no_match_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertFalse { repository.checkCryptographicData(charArrayOf('a')) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun check_not_generated_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.checkCryptographicData(password) }

        repository.generateCryptographicData(password)
        repository.reset()
        assertThrows<OSCryptoError> { repository.checkCryptographicData(password) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun persist_test(): TestResult = runTest {
        repository.generateCryptographicData(password)
        assertDoesNotThrow { repository.setMainCryptographicData() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun persist_not_generated_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.setMainCryptographicData() }

        repository.generateCryptographicData(password)
        repository.reset()
        assertThrows<OSCryptoError> { repository.setMainCryptographicData() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun reEncryptItemKeys_no_key_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.reEncryptItemKeys(listOf()) }
    }

    companion object {
        private val password: CharArray
            get() = "password".toCharArray()
    }
}
