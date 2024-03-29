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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.util.AtomicFile
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.utils.OSCryptoInputStream
import studio.lunabee.onesafe.cryptography.utils.OSCryptoOutputStream
import studio.lunabee.onesafe.cryptography.utils.SafeDataMutableStateFlow
import studio.lunabee.onesafe.cryptography.utils.dataStoreValueDelegate
import studio.lunabee.onesafe.cryptography.utils.safeCryptoArrayDelete
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.use
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.util.UUID
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.time.Duration

private val logger = LBLogger.get<AndroidMainCryptoRepository>()

/**
 * @param dispatcher The coroutine dispatcher used to run cryptographic operation on
 */
@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
@Singleton
class AndroidMainCryptoRepository @Inject constructor(
    private val crypto: CryptoEngine,
    private val hashEngine: PasswordHashEngine,
    private val biometricEngine: BiometricEngine,
    @DatastoreEngineProvider(DataStoreType.Plain) private val dataStoreEngine: DatastoreEngine,
    private val featureFlags: FeatureFlags,
    private val randomKeyProvider: RandomKeyProvider,
    private val mapper: CryptoDataMapper,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher,
) : MainCryptoRepository {

    private val masterKeyFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED,
        nullableCode = OSCryptoError.Code.MASTER_KEY_NOT_LOADED,
    )
    private var masterKey: ByteArray? by safeCryptoArrayDelete(masterKeyFlow)

    private val searchIndexKeyFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = OSCryptoError.Code.SEARCH_INDEX_KEY_ALREADY_LOADED,
        nullableCode = OSCryptoError.Code.SEARCH_INDEX_KEY_NOT_LOADED,
    )
    private var searchIndexKey: ByteArray? by safeCryptoArrayDelete(searchIndexKeyFlow)

    private val bubblesMasterKeyFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = OSCryptoError.Code.BUBBLES_MASTER_KEY_ALREADY_LOADED,
        nullableCode = OSCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED,
    )
    private var bubblesMasterKey: ByteArray? by safeCryptoArrayDelete(bubblesMasterKeyFlow)

    private val itemEditionKeyFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = OSCryptoError.Code.ITEM_EDITION_KEY_ALREADY_LOADED,
        nullableCode = OSCryptoError.Code.ITEM_EDITION_KEY_NOT_LOADED,
    )
    private var itemEditionKey: ByteArray? by safeCryptoArrayDelete(itemEditionKeyFlow)

    override fun isCryptoDataInMemoryFlow(): Flow<Boolean> {
        val flows = mutableListOf(masterKeyFlow, searchIndexKeyFlow, itemEditionKeyFlow)
        return combine(flows) { keys ->
            keys.all { it != null }
        }
            .flowOn(dispatcher)
            .distinctUntilChanged()
    }

    @OptIn(FlowPreview::class)
    override suspend fun isCryptoDataInMemory(timeout: Duration): Boolean {
        return withContext(dispatcher) {
            when (timeout) {
                in -Duration.INFINITE..Duration.ZERO -> isCryptoDataInMemoryFlow().first()
                Duration.INFINITE -> isCryptoDataInMemoryFlow().filter { it }.first()
                else -> {
                    try {
                        isCryptoDataInMemoryFlow()
                            .filter { it }
                            .timeout(timeout)
                            .first()
                    } catch (e: TimeoutCancellationException) {
                        false
                    }
                }
            }
        }
    }

    private var saltDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_MASTER_SALT,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.SALT_ALREADY_GENERATED,
    )

    private var masterKeyTestDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_MASTER_KEY_TEST,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.MASTER_KEY_ALREADY_GENERATED,
    )

    private var searchIndexKeyDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_SEARCH_INDEX_KEY,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.SEARCH_INDEX_KEY_ALREADY_GENERATED,
    )

    private var itemEditionKeyDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_ITEM_EDITION_KEY,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.ITEM_EDITION_KEY_ALREADY_GENERATED,
    )

    private var bubblesMasterKeyDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_BUBBLES_CONTACT_KEY,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.BUBBLES_CONTACT_KEY_ALREADY_GENERATED,
    )

    override suspend fun hasMasterSalt(): Boolean = withContext(dispatcher) { saltDataStore != null }

    override suspend fun getCurrentSalt(): ByteArray = withContext(dispatcher) { saltDataStore!! }

    override suspend fun resetCryptography(): Unit = withContext(dispatcher) {
        dataStoreEngine.clearDataStore()
        unloadMasterKeys()
    }

    override suspend fun unloadMasterKeys(): Unit = withContext(dispatcher) {
        masterKey = null
        searchIndexKey = null
        bubblesMasterKey = null
        itemEditionKey = null
        logger.v("cryptographic keys unloaded")
    }

    override suspend fun storeMasterKeyAndSalt(key: ByteArray, salt: ByteArray): Unit = withContext(dispatcher) {
        if (saltDataStore != null) {
            throw OSCryptoError(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED)
        }
        saltDataStore = salt.copyOf()
        masterKey = key.copyOf()
        logger.v("cryptographic keys stored")

        masterKeyTestDataStore = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = masterKey!!,
            associatedData = null,
        ).getOrElse {
            throw OSCryptoError.Code.MASTER_KEY_TEST_ENCRYPTION_FAILED.get(cause = it)
        }
        generateIndexKey()
        generateItemEditionKey()
        if (featureFlags.bubbles().first()) {
            generateBubblesKey()
        }
    }

    override suspend fun overrideMasterKeyAndSalt(key: ByteArray, salt: ByteArray): Unit = withContext(dispatcher) {
        key.copyInto(masterKey!!)

        dataStoreEngine.editValue(value = salt, key = DATASTORE_MASTER_SALT)
        val masterKeyTestValue = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = masterKey!!,
            associatedData = null,
        ).getOrElse {
            throw OSCryptoError.Code.MASTER_KEY_TEST_ENCRYPTION_FAILED.get(cause = it)
        }
        dataStoreEngine.editValue(value = masterKeyTestValue, key = DATASTORE_MASTER_KEY_TEST)
        reEncryptIndexKey()
        reEncryptItemEditionKey()
        if (featureFlags.bubbles().first()) {
            reEncryptBubblesContactKey()
        }
    }

    override suspend fun loadMasterKeyFromBiometric(cipher: Cipher): Unit = withContext(dispatcher) {
        masterKey = biometricEngine.retrieveKey(cipher)
        retrieveKeyForIndex()
        retrieveKeyForEdition()
        if (featureFlags.bubbles().first()) {
            retrieveKeyForBubblesContact()
        }
        logger.v("cryptographic keys loaded using biometric")
    }

    override suspend fun retrieveMasterKeyFromBiometric(cipher: Cipher): ByteArray = withContext(dispatcher) {
        biometricEngine.retrieveKey(cipher)
    }

    override suspend fun loadMasterKeyExternal(masterKey: ByteArray): Unit = withContext(dispatcher) {
        this@AndroidMainCryptoRepository.masterKey = masterKey.copyOf()
        retrieveKeyForIndex()
        retrieveKeyForEdition()
        if (featureFlags.bubbles().first()) {
            retrieveKeyForBubblesContact()
        }
        logger.v("cryptographic keys externally loaded")
    }

    private suspend fun generateIndexKey(): Unit = withContext(dispatcher) {
        searchIndexKeyDataStore = randomKeyProvider().use { keyData ->
            searchIndexKey = keyData.copyOf()
            crypto.encrypt(keyData, masterKey!!, null)
        }.getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun generateItemEditionKey(): Unit = withContext(dispatcher) {
        itemEditionKeyDataStore = randomKeyProvider().use { keyData ->
            itemEditionKey = keyData.copyOf()
            crypto.encrypt(keyData, masterKey!!, null)
        }.getOrElse {
            throw OSCryptoError.Code.ITEM_EDITION_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    override suspend fun generateBubblesKey(): Unit = withContext(dispatcher) {
        bubblesMasterKeyDataStore = randomKeyProvider().use { keyData ->
            bubblesMasterKey = keyData.copyOf()
            crypto.encrypt(keyData, masterKey!!, null)
        }.getOrElse {
            throw OSCryptoError.Code.BUBBLES_MASTER_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun reEncryptIndexKey(): Unit = withContext(dispatcher) {
        val key = crypto.encrypt(searchIndexKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
        dataStoreEngine.editValue(value = key, key = DATASTORE_SEARCH_INDEX_KEY)
    }

    private suspend fun reEncryptItemEditionKey(): Unit = withContext(dispatcher) {
        val key = crypto.encrypt(itemEditionKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_EDITION_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
        dataStoreEngine.editValue(value = key, key = DATASTORE_ITEM_EDITION_KEY)
    }

    private suspend fun retrieveKeyForIndex(): Unit = withContext(dispatcher) {
        searchIndexKey = crypto.decrypt(searchIndexKeyDataStore!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_DECRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun retrieveKeyForEdition(): Unit = withContext(dispatcher) {
        if (itemEditionKeyDataStore != null) {
            itemEditionKey = crypto.decrypt(itemEditionKeyDataStore!!, masterKey!!, null).getOrElse {
                throw OSCryptoError.Code.ITEM_EDITION_KEY_DECRYPTION_FAIL.get(cause = it)
            }
        } else {
            generateItemEditionKey()
        }
    }

    private suspend fun retrieveKeyForBubblesContact(): Unit = withContext(dispatcher) {
        if (bubblesMasterKeyDataStore != null) {
            bubblesMasterKey = crypto.decrypt(bubblesMasterKeyDataStore!!, masterKey!!, null).getOrElse {
                throw OSCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL.get(cause = it)
            }
        } else {
            generateBubblesKey()
        }
    }

    override suspend fun deleteBubblesCrypto(): Unit = withContext(dispatcher) {
        bubblesMasterKey = null
        dataStoreEngine.editValue(null, DATASTORE_BUBBLES_CONTACT_KEY)
    }

    private suspend fun reEncryptBubblesContactKey(): Unit = withContext(dispatcher) {
        val key = crypto.encrypt(bubblesMasterKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.BUBBLES_CONTACT_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
        dataStoreEngine.editValue(value = key, key = DATASTORE_BUBBLES_CONTACT_KEY)
    }

    override suspend fun enableBiometric(biometricCipher: Cipher): Unit = withContext(dispatcher) {
        try {
            biometricEngine.storeKey(masterKey!!, biometricCipher)
        } catch (osError: OSCryptoError) {
            if (osError.code == OSCryptoError.Code.MASTER_KEY_NOT_LOADED) {
                biometricEngine.disableBiometric()
                throw osError
            }
        }
    }

    override suspend fun loadMasterKeyFromPassword(password: CharArray): Unit = withContext(dispatcher) {
        password.use {
            retrieveMasterKeyFromPassword(password)?.let {
                masterKey = it
                retrieveKeyForIndex()
                retrieveKeyForEdition()
                if (featureFlags.bubbles().first()) {
                    retrieveKeyForBubblesContact()
                }
                logger.v("cryptographic keys loaded using password")
            }
        }
    }

    override suspend fun testPassword(password: CharArray): Boolean = withContext(dispatcher) {
        retrieveMasterKeyFromPassword(password)?.randomize() != null
    }

    private suspend fun retrieveMasterKeyFromPassword(password: CharArray): ByteArray? = withContext(dispatcher) {
        password.use {
            val salt = saltDataStore ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
            salt.use {
                val encMasterKeyTest = masterKeyTestDataStore ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
                hashEngine.deriveKey(password, salt).use { masterKey ->
                    try {
                        val plainMasterKeyTest = crypto.decrypt(encMasterKeyTest, masterKey, null)
                            .getOrThrow()
                            .decodeToString()
                        val isPasswordOk = plainMasterKeyTest == MASTER_KEY_TEST_VALUE
                        if (isPasswordOk) {
                            masterKey.copyOf()
                        } else {
                            null
                        }
                    } catch (e: GeneralSecurityException) {
                        throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD, cause = e)
                    }
                }
            }
        }
    }

    override suspend fun generateKeyForItemId(itemId: UUID): SafeItemKey = withContext(dispatcher) {
        randomKeyProvider().use { keyData ->
            val encryptedKey = crypto.encrypt(keyData, masterKey!!, null).getOrElse {
                throw OSCryptoError.Code.ITEM_KEY_ENCRYPTION_FAIL.get(cause = it)
            }
            SafeItemKey(itemId, encryptedKey)
        }
    }

    override suspend fun importItemKey(rawKeyValue: ByteArray, keyId: UUID): SafeItemKey = withContext(dispatcher) {
        val encKeyValue = crypto.encrypt(rawKeyValue, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
        SafeItemKey(id = keyId, encValue = encKeyValue)
    }

    override suspend fun <Data : Any> decrypt(key: SafeItemKey, decryptEntry: DecryptEntry<Data>): Data = withContext(dispatcher) {
        try {
            doDecrypt(
                key = key,
                decrypt = { rawKey ->
                    crypto.decrypt(decryptEntry.data, rawKey, null).getOrThrow()
                },
                clazz = decryptEntry.clazz,
                mapBlock = decryptEntry.mapBlock,
            )
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun decrypt(key: SafeItemKey, decryptEntries: List<DecryptEntry<out Any>?>): List<Any?> = withContext(dispatcher) {
        try {
            crypto.decrypt(key.encValue, masterKey!!, null).getOrThrow().use { rawKey ->
                decryptEntries.map { decryptEntry ->
                    if (decryptEntry == null) {
                        null
                    } else {
                        val rawData = crypto.decrypt(decryptEntry.data, rawKey, null).getOrThrow()
                        mapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
                    }
                }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any?, Out : Any> decryptWithData(
        key: SafeItemKey,
        decryptEntries: List<Pair<Data, DecryptEntry<out Out>?>>,
    ): List<Pair<Data, Out?>> = withContext(dispatcher) {
        try {
            crypto.decrypt(key.encValue, masterKey!!, null).getOrThrow().use { rawKey ->
                decryptEntries.map { (data, decryptEntry) ->
                    if (decryptEntry == null) {
                        data to null
                    } else {
                        val rawData = crypto.decrypt(decryptEntry.data, rawKey, null).getOrThrow()
                        data to mapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
                    }
                }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any> decrypt(
        file: File,
        key: SafeItemKey,
        clazz: KClass<Data>,
        mapper: (ByteArray.() -> Data)?,
    ): Data = withContext(dispatcher) {
        val aFile = AtomicFile(file)
        doDecrypt(
            key = key,
            decrypt = { rawKey ->
                crypto.decrypt(aFile, rawKey, null).getOrElse {
                    throw OSCryptoError.Code.FILE_DECRYPTION_FAIL.get(cause = it)
                }
            },
            clazz = clazz,
            mapBlock = mapper,
        )
    }

    override suspend fun getDecryptStream(cipherFile: File, key: SafeItemKey): InputStream = withContext(dispatcher) {
        crypto.decrypt(key.encValue, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_KEY_DECRYPTION_FAIL.get(cause = it)
        }.use { rawKey ->
            val cryptoStream = crypto.getDecryptStream(AtomicFile(cipherFile), rawKey, null)
            OSCryptoInputStream(cryptoStream)
        }
    }

    override suspend fun getEncryptStream(cipherFile: File, key: SafeItemKey): OutputStream = withContext(dispatcher) {
        crypto.decrypt(key.encValue, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_KEY_DECRYPTION_FAIL.get(cause = it)
        }.use { rawKey ->
            val cryptoStream = crypto.getEncryptStream(cipherFile, rawKey, null)
            OSCryptoOutputStream(cryptoStream)
        }
    }

    override suspend fun getFileEditionEncryptStream(plainFile: File): OutputStream = withContext(dispatcher) {
        val encryptStream = crypto.getEncryptStream(plainFile, itemEditionKey!!, null)
        OSCryptoOutputStream(encryptStream)
    }

    override suspend fun getFileEditionDecryptStream(encFile: File): InputStream = withContext(dispatcher) {
        val decryptStream = crypto.getDecryptStream(AtomicFile(encFile), itemEditionKey!!, null)
        OSCryptoInputStream(decryptStream)
    }

    override suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntry: EncryptEntry<Data>): ByteArray = withContext(dispatcher) {
        val data = encryptEntry.data
        val mapBlock = encryptEntry.mapBlock

        val rawData = mapper(mapBlock, data)

        try {
            crypto.decrypt(key.encValue, masterKey!!, null).getOrThrow().use { rawKey ->
                crypto.encrypt(rawData, rawKey, null).getOrThrow()
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any> encrypt(
        key: SafeItemKey,
        encryptEntries: List<EncryptEntry<Data>?>,
    ): List<ByteArray?> = withContext(dispatcher) {
        val rawDataList = encryptEntries.map { entry ->
            entry?.let { encryptEntry ->
                mapper(encryptEntry.mapBlock, encryptEntry.data)
            }
        }

        try {
            crypto.decrypt(key.encValue, masterKey!!, null).getOrThrow().use { rawKey ->
                rawDataList.map { data ->
                    data?.let { crypto.encrypt(it, rawKey, null).getOrThrow() }
                }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun encrypt(outputStream: OutputStream, key: ByteArray): OutputStream = withContext(dispatcher) {
        crypto.getCipherOutputStream(outputStream, key, null)
    }

    override suspend fun encryptIndexWord(indexWordEntry: List<PlainIndexWordEntry>): List<IndexWordEntry> = withContext(dispatcher) {
        indexWordEntry.map { wordEntry ->
            val encWord = crypto.encrypt(
                plainData = wordEntry.word.encodeToByteArray(),
                key = searchIndexKey!!,
                associatedData = null,
            ).getOrElse { err -> throw OSCryptoError.Code.INDEX_WORD_ENCRYPTION_FAIL.get(cause = err) }
            IndexWordEntry(
                encWord,
                wordEntry.itemMatch,
                wordEntry.fieldMatch,
            )
        }
    }

    override suspend fun encryptRecentSearch(plainRecentSearch: List<String>): List<ByteArray> = withContext(dispatcher) {
        plainRecentSearch.map { element ->
            crypto.encrypt(plainData = element.encodeToByteArray(), key = searchIndexKey!!, associatedData = null).getOrElse {
                throw OSCryptoError.Code.RECENT_SEARCH_ENCRYPTION_FAIL.get(cause = it)
            }
        }
    }

    override suspend fun decryptRecentSearch(encRecentSearch: List<ByteArray>): List<String> = withContext(dispatcher) {
        encRecentSearch.map { cypherData ->
            crypto.decrypt(cypherData, searchIndexKey!!, null).getOrElse {
                throw OSCryptoError.Code.RECENT_SEARCH_DECRYPTION_FAIL.get(cause = it)
            }.decodeToString()
        }
    }

    override suspend fun decryptIndexWord(encIndexWordEntry: List<IndexWordEntry>): List<PlainIndexWordEntry> = withContext(dispatcher) {
        encIndexWordEntry.map { wordEntry ->
            PlainIndexWordEntry(
                crypto.decrypt(wordEntry.encWord, searchIndexKey!!, null).getOrElse {
                    throw OSCryptoError.Code.INDEX_WORD_DECRYPTION_FAIL.get(cause = it)
                }.decodeToString(),
                wordEntry.itemMatch,
                wordEntry.fieldMatch,
            )
        }
    }

    override suspend fun reEncryptItemKey(itemKey: SafeItemKey, key: ByteArray): Unit = withContext(dispatcher) {
        crypto.decrypt(cipherData = itemKey.encValue, key = masterKey!!, associatedData = null).getOrElse {
            throw OSCryptoError.Code.ITEM_KEY_DECRYPTION_FAIL.get(cause = it)
        }.use { plainKey ->
            crypto.encrypt(plainKey, key, null).getOrElse {
                throw OSCryptoError.Code.ITEM_EDITION_KEY_ENCRYPTION_FAIL.get(cause = it)
            }.copyInto(itemKey.encValue)
        }
    }

    override suspend fun encryptBubbles(data: ByteArray): ByteArray = withContext(dispatcher) {
        try {
            crypto.encrypt(data, bubblesMasterKey!!, null).getOrThrow()
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun decryptBubbles(data: ByteArray): ByteArray = withContext(dispatcher) {
        try {
            crypto.decrypt(data, bubblesMasterKey!!, null).getOrThrow()
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    private suspend fun <Data : Any> doDecrypt(
        key: SafeItemKey,
        decrypt: suspend (rawKey: ByteArray) -> ByteArray,
        clazz: KClass<Data>,
        mapBlock: (ByteArray.() -> Data)?,
    ): Data {
        val rawData = try {
            crypto.decrypt(key.encValue, masterKey!!, null).getOrElse {
                throw OSCryptoError.Code.ITEM_KEY_DECRYPTION_FAIL.get(cause = it)
            }.use { rawKey ->
                decrypt(rawKey)
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }

        return mapper(mapBlock, rawData, clazz)
    }

    companion object {
        private const val DATASTORE_SEARCH_INDEX_KEY = "f0ab7671-5314-41dc-9f57-3c689180ab33"
        private const val DATASTORE_ITEM_EDITION_KEY = "6f596059-24b8-429e-bfe4-daea05310de8"
        const val DATASTORE_MASTER_SALT: String = "b282a019-4337-45a3-8bf6-da657ad39a6c"
        private const val DATASTORE_MASTER_KEY_TEST = "f9e3fa44-2f54-4246-8ba6-2784a18b63ea"
        private const val DATASTORE_BUBBLES_CONTACT_KEY: String = "2b96478c-cbd4-4150-b591-6fe5a4dffc5f"

        private const val MASTER_KEY_TEST_VALUE = "44c5dac9-17ba-4690-9275-c7471b2e0582"
    }
}
