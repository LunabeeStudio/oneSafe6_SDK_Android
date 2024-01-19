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

import android.util.Base64
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@HiltAndroidTest
class AndroidMainCryptoRepositoryTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var crypto: CryptoEngine

    @Inject
    internal lateinit var hashEngine: PasswordHashEngine

    @Inject
    internal lateinit var randomKeyProvider: RandomKeyProvider

    @Inject
    internal lateinit var dataStore: EncryptedDataStoreEngine

    @Inject
    internal lateinit var mapper: CryptoDataMapper

    private val featureFlags: FeatureFlags = mockk {
        every { this@mockk.bubbles() } returns flowOf(false)
    }

    private val repository: AndroidMainCryptoRepository by lazy {
        AndroidMainCryptoRepository(crypto, hashEngine, mockk(), dataStore, featureFlags, randomKeyProvider, mapper)
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            this@AndroidMainCryptoRepositoryTest.repository.resetCryptography()
        }
    }

    @Test
    fun storeMasterKeyAndSalt_already_loaded_test(): TestResult = runTest {
        this@AndroidMainCryptoRepositoryTest.repository.storeMasterKeyAndSalt(key, salt)
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.storeMasterKeyAndSalt(key, salt)
        }
        assertEquals(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED, error.code)
    }

    @Test
    fun storeMasterKeyAndSalt_already_saved_test(): TestResult = runTest {
        loadMasterKey()
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.storeMasterKeyAndSalt(key, salt)
        }
        assertEquals(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED, error.code)
    }

    @Test
    fun loadMasterKey_no_password_stored_test(): TestResult = runTest {
        assertThrows<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(password.toCharArray())
        }
    }

    @Test
    fun loadMasterKey_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertDoesNotThrow { this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(password.toCharArray()) }
    }

    @Test
    fun loadMasterKey_already_loaded_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertDoesNotThrow { this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(password.toCharArray()) }
        val error = assertThrows<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(
                password.toCharArray(),
            )
        }
        assertEquals(OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED, error.code)
    }

    @Test
    fun loadMasterKey_wrong_password_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertThrows<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(charArrayOf('a'))
        }
    }

    @Test
    fun getCurrentSalt_no_master_key_test(): TestResult = runTest {
        assertThrows<NullPointerException> { this@AndroidMainCryptoRepositoryTest.repository.getCurrentSalt() }
    }

    @Test
    fun no_master_key_test(): TestResult = runTest {
        val error1 = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        }
        assertEquals(error1.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)

        val error2 = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encrypt(mockk(relaxed = true), EncryptEntry(""))
        }
        assertEquals(error2.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)

        val error3 = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.decrypt(mockk(relaxed = true), DecryptEntry(byteArrayOf(), Unit::class))
        }
        assertEquals(error3.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)
    }

    @Test
    fun generateKeyForItem_test(): TestResult = runTest {
        loadMasterKey()
        this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
    }

    @Test
    fun deleteMasterKey_test(): TestResult = runTest {
        loadMasterKey()
        this@AndroidMainCryptoRepositoryTest.repository.resetCryptography()
        this@AndroidMainCryptoRepositoryTest.repository.resetCryptography() // test safe call twice
    }

    @Test
    fun encrypt_decrypt_string_test(): TestResult = runTest {
        loadMasterKey()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encText = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        val decText = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encText, String::class))
        assertEquals("plain_text", decText)
    }

    @Test
    fun encrypt_decrypt_int_test(): TestResult = runTest {
        loadMasterKey()
        val num = Random.nextInt()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encNum = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(num))
        val decNum = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encNum, Int::class))
        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_decrypt_SafeItemFieldKind_test(): TestResult = runTest {
        loadMasterKey()
        val kind = SafeItemFieldKind.Email
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encKind = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))
        assertEquals(kind, decKind)
    }

    @Test
    fun encrypt_decrypt_unknown_SafeItemFieldKind_test(): TestResult = runTest {
        loadMasterKey()
        val kind = SafeItemFieldKind.Unknown("unknown_type")
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encKind = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))

        assertIs<SafeItemFieldKind.Unknown>(decKind)
        assertEquals(kind.id, decKind.id)
    }

    @Test
    fun decrypt_no_mapper_test(): TestResult = runTest {
        loadMasterKey()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encText = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encText, Unit::class))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun encrypt_no_mapper_test(): TestResult = runTest {
        loadMasterKey()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(Unit))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun decrypt_wrong_key_test(): TestResult = runTest {
        loadMasterKey()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val wrongKey = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[1])
        val encText = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.decrypt(wrongKey, DecryptEntry(encText, Unit::class))
        }
        assertEquals(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, error.code)
    }

    @Test
    fun encrypt_bad_key_test(): TestResult = runTest {
        loadMasterKey()
        val key = SafeItemKey(testUUIDs[0], ByteArray(12) { it.toByte() })
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        }
        assertEquals(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, error.code)
    }

    @Test
    fun encrypt_reload_master_key_decrypt_test(): TestResult = runTest {
        loadMasterKey()
        val num = Random.nextInt()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encNum = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(num))

        unloadMasterKey()
        this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(password.toCharArray())
        val decNum = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encNum, Int::class))

        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_and_decrypt_index_entry_test(): TestResult = runTest {
        loadMasterKey()
        val initialEntries = listOf(
            PlainIndexWordEntry(
                "word",
                UUID.randomUUID(),
                null,
            ),
        )
        val encryptedEntries: List<IndexWordEntry> = this@AndroidMainCryptoRepositoryTest.repository.encryptIndexWord(initialEntries)
        val decryptedEntries: List<PlainIndexWordEntry> = this@AndroidMainCryptoRepositoryTest.repository.decryptIndexWord(encryptedEntries)
        assertContentEquals(initialEntries, decryptedEntries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collect_crypto_data_missing_test_test(): TestResult = runTest {
        val values = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            this@AndroidMainCryptoRepositoryTest.repository.isCryptoDataInMemoryFlow().toList(values)
        }
        assertFalse(values[0])
        loadMasterKey()
        assertTrue(values[1])
        unloadMasterKey()
        assertFalse(values[2])
        assertEquals(3, values.size)
    }

    @Test
    fun overrideMasterKeyAndSalt_test(): TestResult = runTest {
        this@AndroidMainCryptoRepositoryTest.repository.storeMasterKeyAndSalt(ByteArray(32) { it.toByte() }, byteArrayOf(1))
        this@AndroidMainCryptoRepositoryTest.repository.overrideMasterKeyAndSalt(key, salt)
        unloadMasterKey()
        assertDoesNotThrow { this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyFromPassword(password.toCharArray()) }
    }

    @Test
    fun bubbles_contacts_key_not_set_if_feature_not_enabled_test(): TestResult = runTest {
        loadMasterKey()
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encryptBubbles(ByteArray(1))
        }
        assertEquals(OSCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED, error.code)
    }

    @Test
    fun encrypt_decrypt_for_bubbles_test(): TestResult = runTest {
        every { featureFlags.bubbles() } returns flowOf(true)
        this@AndroidMainCryptoRepositoryTest.repository.storeMasterKeyAndSalt(key, salt)
        val plainData = "contactName"
        val encryptedData = this@AndroidMainCryptoRepositoryTest.repository.encryptBubbles(plainData.encodeToByteArray())
        val decryptedData = this@AndroidMainCryptoRepositoryTest.repository.decryptBubbles(encryptedData)
        assertEquals(plainData, decryptedData.decodeToString())
    }

    private suspend fun loadMasterKey(repository: AndroidMainCryptoRepository = this.repository) {
        repository.storeMasterKeyAndSalt(key, salt)
    }

    private fun unloadMasterKey() {
        this.repository.unloadMasterKeys()
    }

    companion object {
        private const val password = "password"
        private val key = Base64.decode("80ro8nx2VzXRkKl4G43W4uvdIrVnUgeZm1Zk86KN2PU=", Base64.NO_WRAP)
        private val salt = byteArrayOf(0)
    }
}
