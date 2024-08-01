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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.lang.Thread.UncaughtExceptionHandler
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    internal lateinit var mapper: CryptoDataMapper

    @Inject
    internal lateinit var safeRepository: SafeRepository

    @Inject
    @CryptoDispatcher
    internal lateinit var cryptoDispatcher: CoroutineDispatcher

    @Inject
    @DatastoreEngineProvider(DataStoreType.Plain)
    internal lateinit var dataStoreEngine: DatastoreEngine

    private val featureFlags: FeatureFlags = mockk {
        every { this@mockk.bubbles() } returns flowOf(false)
    }

    private val repository: AndroidMainCryptoRepository by lazy {
        AndroidMainCryptoRepository(
            crypto = crypto,
            hashEngine = hashEngine,
            biometricEngine = mockk(),
            featureFlags = featureFlags,
            randomKeyProvider = randomKeyProvider,
            mapper = mapper,
            dispatcher = cryptoDispatcher,
            safeRepository = safeRepository,
        )
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            resetCryptography()
        }
    }

    private suspend fun resetCryptography() {
        dataStoreEngine.clearDataStore()
        unloadMasterKey()
        safeRepository.deleteSafe(firstSafeId)
    }

    @Test
    fun storeMasterKeyAndSalt_already_saved_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyExternal(masterKey)
        }
        assertEquals(OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED, error.code)
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
        generateAndLoadCrypto()
        this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
    }

    @Test
    fun encrypt_decrypt_string_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encText = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        val decText = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encText, String::class))
        assertEquals("plain_text", decText)
    }

    @Test
    fun encrypt_decrypt_int_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val num = Random.nextInt()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encNum = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(num))
        val decNum = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encNum, Int::class))
        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_decrypt_SafeItemFieldKind_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val kind = SafeItemFieldKind.Email
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encKind = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))
        assertEquals(kind, decKind)
    }

    @Test
    fun encrypt_decrypt_unknown_SafeItemFieldKind_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val kind = SafeItemFieldKind.Unknown("unknown_type")
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encKind = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(kind, SafeItemFieldKind::toByteArray))
        val decKind = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encKind, SafeItemFieldKind::class))

        assertIs<SafeItemFieldKind.Unknown>(decKind)
        assertEquals(kind.id, decKind.id)
    }

    @Test
    fun decrypt_no_mapper_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encText = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encText, Unit::class))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun encrypt_no_mapper_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(Unit))
        }
        assertEquals(OSCryptoError.Code.MISSING_MAPPER, error.code)
    }

    @Test
    fun decrypt_wrong_key_test(): TestResult = runTest {
        generateAndLoadCrypto()
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
        generateAndLoadCrypto()
        val key = SafeItemKey(testUUIDs[0], ByteArray(12) { it.toByte() })
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry("plain_text"))
        }
        assertEquals(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, error.code)
    }

    @Test
    fun encrypt_reload_master_key_decrypt_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val num = Random.nextInt()
        val key = this@AndroidMainCryptoRepositoryTest.repository.generateKeyForItemId(testUUIDs[0])
        val encNum = this@AndroidMainCryptoRepositoryTest.repository.encrypt(key, EncryptEntry(num))

        unloadMasterKey()
        this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyExternal(masterKey)
        val decNum = this@AndroidMainCryptoRepositoryTest.repository.decrypt(key, DecryptEntry(encNum, Int::class))

        assertEquals(num, decNum)
    }

    @Test
    fun encrypt_and_decrypt_index_entry_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val initialWords = listOf(
            "word",
        )
        val encryptedEntries: List<ByteArray> = this@AndroidMainCryptoRepositoryTest.repository.encryptIndexWord(initialWords)
        val decryptedEntries: List<String> = this@AndroidMainCryptoRepositoryTest.repository.decryptIndexWord(encryptedEntries)
        assertContentEquals(initialWords, decryptedEntries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun collect_crypto_data_missing_test(): TestResult = runTest {
        val values = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            this@AndroidMainCryptoRepositoryTest.repository.isCryptoDataInMemoryFlow().toList(values)
        }

        this@AndroidMainCryptoRepositoryTest.repository.isCryptoDataInMemoryFlow().filter { !it }.first()
        assertFalse(values[0])
        generateAndLoadCrypto()
        this@AndroidMainCryptoRepositoryTest.repository.isCryptoDataInMemoryFlow().filter { it }.first()
        assertTrue(values[1])
        unloadMasterKey()
        this@AndroidMainCryptoRepositoryTest.repository.isCryptoDataInMemoryFlow().filter { !it }.first()
        assertFalse(values[2])

        assertEquals(3, values.size)
    }

    @Test
    fun isCryptoDataInMemory_test(): TestResult = runTest {
        assertFalse(repository.isCryptoDataInMemory(Duration.ZERO))
        assertFalse(repository.isCryptoDataInMemory(10.milliseconds))
        generateAndLoadCrypto()
        assertTrue(repository.isCryptoDataInMemory(Duration.ZERO))
        assertTrue(repository.isCryptoDataInMemory(10.milliseconds))
        unloadMasterKey()
        launch { assertFalse(repository.isCryptoDataInMemory(Duration.ZERO)) }
        launch { assertTrue(repository.isCryptoDataInMemory(1.seconds)) }
        launch(Dispatchers.Default) {
            delay(100.milliseconds)
            repository.loadMasterKeyExternal(masterKey)
        }
    }

    @Test
    fun regenerateAndOverrideLoadedCrypto_test(): TestResult = runTest {
        val key = ByteArray(32) { it.toByte() }
        val newSafeCrypto = this@AndroidMainCryptoRepositoryTest.repository.generateCrypto(
            key = key,
            salt = byteArrayOf(1),
            biometricCipher = null,
        )
        val safeCrypto = SafeCrypto(
            id = firstSafeId,
            salt = newSafeCrypto.salt,
            encTest = newSafeCrypto.encTest,
            encIndexKey = newSafeCrypto.encIndexKey,
            encBubblesKey = newSafeCrypto.encBubblesKey,
            encItemEditionKey = newSafeCrypto.encItemEditionKey,
            biometricCryptoMaterial = null,
        )
        safeRepository.insertSafe(
            safeCrypto = safeCrypto,
            safeSettings = OSTestUtils.safeSettings(),
            appVisit = OSTestUtils.appVisit(),
        )
        this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyExternal(key)

        val newSafeCrypto2 = this@AndroidMainCryptoRepositoryTest.repository.regenerateAndOverrideLoadedCrypto(
            masterKey,
            salt,
            null,
        )
        val safeCrypto2 = SafeCrypto(
            id = firstSafeId,
            salt = newSafeCrypto2.salt,
            encTest = newSafeCrypto2.encTest,
            encIndexKey = newSafeCrypto2.encIndexKey,
            encBubblesKey = newSafeCrypto2.encBubblesKey,
            encItemEditionKey = newSafeCrypto2.encItemEditionKey,
            biometricCryptoMaterial = null,
        )
        safeRepository.updateSafeCrypto(safeCrypto = safeCrypto2)

        unloadMasterKey()
        assertDoesNotThrow { this@AndroidMainCryptoRepositoryTest.repository.loadMasterKeyExternal(masterKey) }
    }

    @Test
    fun bubbles_contacts_key_not_set_if_feature_not_enabled_test(): TestResult = runTest {
        generateAndLoadCrypto()
        val error = assertFailsWith<OSCryptoError> {
            this@AndroidMainCryptoRepositoryTest.repository.encryptBubbles(ByteArray(1))
        }
        assertEquals(OSCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED, error.code)
    }

    @Test
    fun encrypt_decrypt_for_bubbles_test(): TestResult = runTest {
        every { featureFlags.bubbles() } returns flowOf(true)
        generateAndLoadCrypto()
        val plainData = "contactName"
        val encryptedData = this@AndroidMainCryptoRepositoryTest.repository.encryptBubbles(plainData.encodeToByteArray())
        val decryptedData = this@AndroidMainCryptoRepositoryTest.repository.decryptBubbles(encryptedData)
        assertEquals(plainData, decryptedData.decodeToString())
    }

    /**
     * ~Reproduce AEADBadTagException which happens during index key decryption by flooding loadMasterKeyExternal and unloadMasterKey
     * concurrently during 10 seconds. Ignore expected errors until AEADBadTagException is thrown (actually IllegalBlockSizeException is
     * thrown, but it seems to be the same cause). Use thread API directly, easier to manage in test.
     *
     * @see <a href="https://www.notion.so/lunabeestudio/Refacto-cryto-pour-viter-la-concurrence-2a4dec45f9a04c58ad09c49ed6ea5015?pvs=4">
     *     Notion</a>
     */
    @Test
    fun stress_unloadMasterKeys_vs_loadMasterKeyExternal_test() {
        var error: Throwable? = null
        val maxTime = System.currentTimeMillis() + 10_000
        val repeat = 100
        runTest {
            generateAndLoadCrypto()
        }

        val loadThreads = List(10) { threadIdx ->
            thread(start = false) {
                println("Run loadMasterKeyExternal $threadIdx on thread #${Thread.currentThread().id}")
                runBlocking {
                    repeat(repeat) {
                        println("run $threadIdx $it")
                        if (error != null) {
                            return@runBlocking
                        } else if (System.currentTimeMillis() > maxTime) {
                            println("Thread #${Thread.currentThread().id} timeout")
                            return@runBlocking
                        }
                        try {
                            repository.loadMasterKeyExternal(masterKey)
                        } catch (e: OSCryptoError) {
                            val expectedErrors = listOf(
                                OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED,
                                OSCryptoError.Code.MASTER_KEY_NOT_LOADED,
                                OSCryptoError.Code.SEARCH_INDEX_KEY_ALREADY_LOADED,
                            )
                            if (e.code !in expectedErrors) throw e
                        }
                    }
                }
            }
        }
        val handler = UncaughtExceptionHandler { _, e ->
            println("Error on on thread #${Thread.currentThread().id}")
            error = e
        }
        loadThreads.forEach { it.setUncaughtExceptionHandler(handler) }
        loadThreads.forEach { it.start() }

        thread {
            println("Run unloadMasterKeys on thread #${Thread.currentThread().id}")
            while (loadThreads.any { it.isAlive }) {
                runBlocking { unloadMasterKey() }
            }
        }
        loadThreads.forEach { it.join() }
        error?.let { throw it }
    }

    private suspend fun generateAndLoadCrypto(repository: AndroidMainCryptoRepository = this.repository) {
        val cryptoSafe = repository.generateCrypto(masterKey, salt, null)
        val safeCrypto = SafeCrypto(
            id = firstSafeId,
            salt = cryptoSafe.salt,
            encTest = cryptoSafe.encTest,
            encIndexKey = cryptoSafe.encIndexKey,
            encBubblesKey = cryptoSafe.encBubblesKey,
            encItemEditionKey = cryptoSafe.encItemEditionKey,
            biometricCryptoMaterial = null,
        )
        safeRepository.insertSafe(
            safeCrypto = safeCrypto,
            safeSettings = OSTestUtils.safeSettings(),
            appVisit = OSTestUtils.appVisit(),
        )
        repository.loadMasterKeyExternal(masterKey)
    }

    private suspend fun unloadMasterKey() {
        this.repository.unloadMasterKeys()
    }

    companion object {
        private val masterKey = Base64.decode("80ro8nx2VzXRkKl4G43W4uvdIrVnUgeZm1Zk86KN2PU=", Base64.NO_WRAP)
        private val salt = byteArrayOf(0)
    }
}
