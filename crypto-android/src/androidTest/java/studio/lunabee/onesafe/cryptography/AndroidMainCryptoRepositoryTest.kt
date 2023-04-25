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
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.ClearIndexWordEntry
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class AndroidMainCryptoRepositoryTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var crypto: CryptoEngine

    @Inject
    internal lateinit var hashEngine: HashEngine

    @Inject
    internal lateinit var saltProvider: SaltProvider

    @Inject
    internal lateinit var itemKeyProvider: ItemKeyProvider

    @Inject
    internal lateinit var dataStore: EncryptedDataStoreEngine

    private val repository: AndroidMainCryptoRepository by lazy {
        AndroidMainCryptoRepository(crypto, hashEngine, mockk(), dataStore, itemKeyProvider)
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            repository.resetCryptography()
        }
    }

    @Test
    fun storeMasterKeyAndSalt_already_loaded_test(): TestResult = runTest {
        repository.storeMasterKeyAndSalt(key, salt)
        val error = assertFailsWith<OSCryptoError> {
            repository.storeMasterKeyAndSalt(key, salt)
        }
        assertEquals(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED, error.code)
    }

    @Test
    fun storeMasterKeyAndSalt_already_saved_test(): TestResult = runTest {
        loadMasterKey()
        val error = assertFailsWith<OSCryptoError> {
            repository.storeMasterKeyAndSalt(key, salt)
        }
        assertEquals(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED, error.code)
    }

    @Test
    fun loadMasterKey_no_password_stored_test(): TestResult = runTest {
        assertThrows<OSCryptoError> { repository.loadMasterKeyFromPassword(password.toCharArray()) }
    }

    @Test
    fun loadMasterKey_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertDoesNotThrow { repository.loadMasterKeyFromPassword(password.toCharArray()) }
    }

    @Test
    fun loadMasterKey_already_loaded_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertDoesNotThrow { repository.loadMasterKeyFromPassword(password.toCharArray()) }
        val error = assertThrows<OSCryptoError> { repository.loadMasterKeyFromPassword(password.toCharArray()) }
        assertEquals(OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED, error.code)
    }

    @Test
    fun loadMasterKey_wrong_password_test(): TestResult = runTest {
        loadMasterKey()
        unloadMasterKey()
        assertThrows<OSCryptoError> { repository.loadMasterKeyFromPassword(charArrayOf('a')) }
    }

    @Test
    fun getCurrentSalt_no_master_key_test(): TestResult = runTest {
        assertThrows<NullPointerException> { repository.getCurrentSalt() }
    }

    @Test
    fun no_master_key_test(): TestResult = runTest {
        val error1 = assertFailsWith<OSCryptoError> {
            repository.generateKeyForItemId(testUUIDs[0])
        }
        assertEquals(error1.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)

        val error2 = assertFailsWith<OSCryptoError> {
            repository.encrypt(mockk(relaxed = true), EncryptEntry(""))
        }
        assertEquals(error2.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)

        val error3 = assertFailsWith<OSCryptoError> {
            repository.decrypt(mockk(relaxed = true), DecryptEntry(byteArrayOf(), Unit::class))
        }
        assertEquals(error3.code, OSCryptoError.Code.MASTER_KEY_NOT_LOADED)
    }

    @Test
    fun generateKeyForItem_test(): TestResult = runTest {
        loadMasterKey()
        repository.generateKeyForItemId(testUUIDs[0])
    }

    @Test
    fun deleteMasterKey_test(): TestResult = runTest {
        loadMasterKey()
        repository.resetCryptography()
        repository.resetCryptography() // test safe call twice
    }

    @Test
    fun encrypt_decrypt_string_test(): TestResult = runTest {
        loadMasterKey()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encText = repository.encrypt(key, EncryptEntry("clear_text"))
        val decText = repository.decrypt(key, DecryptEntry(encText, String::class))
        assertEquals("clear_text", decText)
    }

    @Test
    fun encrypt_decrypt_int_test(): TestResult = runTest {
        loadMasterKey()
        val num = Random.nextInt()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encNum = repository.encrypt(key, EncryptEntry(num))
        val decNum = repository.decrypt(key, DecryptEntry(encNum, Int::class))
        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_decrypt_SafeItemFieldKind_test(): TestResult = runTest {
        loadMasterKey()
        val kind = SafeItemFieldKind.Email
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encKind = repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))
        assertEquals(kind, decKind)
    }

    @Test
    fun encrypt_decrypt_unknown_SafeItemFieldKind_test(): TestResult = runTest {
        loadMasterKey()
        val kind = SafeItemFieldKind.Unknown("unknown_type")
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encKind = repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))

        assertIs<SafeItemFieldKind.Unknown>(decKind)
        assertEquals(kind.id, decKind.id)
    }

    @Test
    fun decrypt_no_mapper_test(): TestResult = runTest {
        loadMasterKey()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encText = repository.encrypt(key, EncryptEntry("clear_text"))
        val error = assertFailsWith<OSCryptoError> {
            repository.decrypt(key, DecryptEntry(encText, Unit::class))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun encrypt_no_mapper_test(): TestResult = runTest {
        loadMasterKey()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val error = assertFailsWith<OSCryptoError> {
            repository.encrypt(key, EncryptEntry(Unit))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun decrypt_wrong_key_test(): TestResult = runTest {
        loadMasterKey()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val wrongKey = repository.generateKeyForItemId(testUUIDs[1])
        val encText = repository.encrypt(key, EncryptEntry("clear_text"))
        val error = assertFailsWith<OSCryptoError> {
            repository.decrypt(wrongKey, DecryptEntry(encText, Unit::class))
        }
        assertEquals(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, error.code)
    }

    @Test
    fun encrypt_bad_key_test(): TestResult = runTest {
        loadMasterKey()
        val key = SafeItemKey(testUUIDs[0], ByteArray(12) { it.toByte() })
        val error = assertFailsWith<OSCryptoError> {
            repository.encrypt(key, EncryptEntry("clear_text"))
        }
        assertEquals(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, error.code)
    }

    @Test
    fun encrypt_reload_master_key_decrypt_test(): TestResult = runTest {
        loadMasterKey()
        val num = Random.nextInt()
        val key = repository.generateKeyForItemId(testUUIDs[0])
        val encNum = repository.encrypt(key, EncryptEntry(num))

        unloadMasterKey()
        repository.loadMasterKeyFromPassword(password.toCharArray())
        val decNum = repository.decrypt(key, DecryptEntry(encNum, Int::class))

        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_and_decrypt_index_entry_test(): TestResult = runTest {
        loadMasterKey()
        val initialEntries = listOf(
            ClearIndexWordEntry(
                "word",
                UUID.randomUUID(),
                null,
            ),
        )
        val encryptedEntries: List<IndexWordEntry> = repository.encryptIndexWord(initialEntries)
        val decryptedEntries: List<ClearIndexWordEntry> = repository.decryptIndexWord(encryptedEntries)
        assertContentEquals(initialEntries, decryptedEntries)
    }

    @Test
    fun collect_crypto_data_missing_test_test(): TestResult = runTest {
        val values = mutableListOf<LBFlowResult<Unit>>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.isCryptoDataInMemory().toList(values)
        }
        assertFailure(values[0])
        loadMasterKey()
        assertSuccess(values[1])
        unloadMasterKey()
        assertFailure(values[2])
        assertEquals(3, values.size)
        collectJob.cancel()
    }

    @Test
    fun overrideMasterKeyAndSalt_test(): TestResult = runTest {
        repository.storeMasterKeyAndSalt(ByteArray(32) { it.toByte() }, byteArrayOf(1))
        repository.overrideMasterKeyAndSalt(key, salt)
        unloadMasterKey()
        assertDoesNotThrow { repository.loadMasterKeyFromPassword(password.toCharArray()) }
    }

    private suspend fun loadMasterKey() {
        repository.storeMasterKeyAndSalt(key, salt)
    }

    private fun unloadMasterKey() {
        OSTestUtils.unloadMasterKey(repository)
    }

    companion object {
        private const val password = "password"
        private val key = Base64.decode("80ro8nx2VzXRkKl4G43W4uvdIrVnUgeZm1Zk86KN2PU=", Base64.NO_WRAP)
        private val salt = byteArrayOf(0)
    }
}
