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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 9:56 AM
 */

package studio.lunabee.onesafe.cryptography.android

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
import studio.lunabee.bubbles.repository.BubblesMainCryptoRepository
import studio.lunabee.onesafe.cryptography.android.utils.OSCryptoInputStream
import studio.lunabee.onesafe.cryptography.android.utils.OSCryptoOutputStream
import studio.lunabee.onesafe.cryptography.android.utils.SafeDataMutableStateFlow
import studio.lunabee.onesafe.cryptography.android.utils.safeCryptoArrayDelete
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.crypto.NewSafeCrypto
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository.Companion.MASTER_KEY_TEST_VALUE
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.jvm.get
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
    private val featureFlags: FeatureFlags,
    private val randomKeyProvider: RandomKeyProvider,
    private val mapper: AndroidCryptoDataMapper,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher,
    private val safeRepository: SafeRepository,
) : MainCryptoRepository, BubblesMainCryptoRepository {

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
        val flows = listOf(masterKeyFlow, searchIndexKeyFlow, itemEditionKeyFlow)
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

    override suspend fun unloadMasterKeys(): Unit = withContext(dispatcher) {
        masterKey = null
        searchIndexKey = null
        bubblesMasterKey = null
        itemEditionKey = null
        logger.v("cryptographic keys unloaded")
    }

    override suspend fun generateCrypto(
        key: ByteArray,
        salt: ByteArray,
        biometricCipher: Cipher?,
    ): NewSafeCrypto = withContext(dispatcher) {
        logger.v("cryptographic keys generated")

        val encIndexKey = generateIndexKey(key)
        val encItemEditionKey = generateItemEditionKey(key)
        val encBubblesKey = if (featureFlags.bubbles()) {
            generateBubblesKey(key)
        } else {
            null
        }

        val testValue = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = key,
            associatedData = null,
        ).getOrElse {
            throw OSCryptoError.Code.MASTER_KEY_TEST_ENCRYPTION_FAILED.get(cause = it)
        }

        val biometricCryptoMaterial = if (biometricCipher != null) {
            biometricEngine.encryptKey(key, biometricCipher)
        } else {
            null
        }

        NewSafeCrypto(
            salt = salt,
            encTest = testValue,
            encIndexKey = encIndexKey,
            encItemEditionKey = encItemEditionKey,
            encBubblesKey = encBubblesKey,
            biometricCryptoMaterial = biometricCryptoMaterial,
        )
    }

    override suspend fun regenerateAndOverrideLoadedCrypto(
        key: ByteArray,
        salt: ByteArray,
        biometricCipher: Cipher?,
    ): NewSafeCrypto = withContext(dispatcher) {
        key.copyInto(masterKey!!)

        val testValue = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = key,
            associatedData = null,
        ).getOrElse {
            throw OSCryptoError.Code.MASTER_KEY_TEST_ENCRYPTION_FAILED.get(cause = it)
        }
        val encIndexKey = reEncryptIndexKey()
        val encItemEditionKey = reEncryptItemEditionKey()
        val encBubblesKey = if (featureFlags.bubbles()) {
            reEncryptBubblesContactKey()
        } else {
            null
        }

        val encBiometricMasterKey = if (biometricCipher != null) {
            biometricEngine.encryptKey(key, biometricCipher)
        } else {
            null
        }

        NewSafeCrypto(
            salt = salt,
            encTest = testValue,
            encIndexKey = encIndexKey,
            encItemEditionKey = encItemEditionKey,
            encBubblesKey = encBubblesKey,
            biometricCryptoMaterial = encBiometricMasterKey,
        )
    }

    // TODO <multisafe> unit test (= test code NO_SAFE_IDENTIFIER_MATCH_KEY)
    private suspend fun getSafeFromMasterKey(): SafeCrypto = withContext(dispatcher) {
        safeRepository.getAllSafeOrderByLastOpenAsc().firstOrNull { identifier ->
            try {
                val plainMasterKeyTest = crypto.decrypt(identifier.encTest, masterKey!!, null)
                    .getOrThrow()
                    .decodeToString()
                plainMasterKeyTest == MASTER_KEY_TEST_VALUE
            } catch (e: GeneralSecurityException) {
                false
            }
        } ?: throw OSCryptoError(OSCryptoError.Code.NO_SAFE_MATCH_KEY)
    }

    override suspend fun loadMasterKeyFromBiometric(safeCrypto: SafeCrypto, cipher: Cipher): Unit = withContext(dispatcher) {
        val encKey = checkNotNull(safeCrypto.biometricCryptoMaterial) {
            "encBiometricMasterKey must not be null"
        }
        masterKey = biometricEngine.decryptKey(encKey, cipher)
        retrieveKeyForIndex(safeCrypto)
        retrieveKeyForEdition(safeCrypto)
        if (featureFlags.bubbles()) {
            retrieveKeyForBubblesContact(safeCrypto)
        }
        logger.v("cryptographic keys loaded using biometric")
    }

    override suspend fun decryptMasterKeyWithBiometric(
        biometricCryptoMaterial: BiometricCryptoMaterial,
        cipher: Cipher,
    ): ByteArray = withContext(dispatcher) {
        biometricEngine.decryptKey(biometricCryptoMaterial, cipher)
    }

    override suspend fun loadMasterKeyExternal(masterKey: ByteArray): Unit = withContext(dispatcher) {
        this@AndroidMainCryptoRepository.masterKey = masterKey.copyOf()
        val safeCrypto = getSafeFromMasterKey()
        retrieveKeyForIndex(safeCrypto)
        retrieveKeyForEdition(safeCrypto)
        if (featureFlags.bubbles()) {
            retrieveKeyForBubblesContact(safeCrypto)
        }
        logger.v("cryptographic keys externally loaded")
    }

    private suspend fun generateIndexKey(key: ByteArray): ByteArray = withContext(dispatcher) {
        randomKeyProvider().use { keyData ->
            crypto.encrypt(keyData, key, null)
        }.getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun generateItemEditionKey(key: ByteArray): ByteArray = withContext(dispatcher) {
        randomKeyProvider().use { keyData ->
            crypto.encrypt(keyData, key, null)
        }.getOrElse {
            throw OSCryptoError.Code.ITEM_EDITION_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun generateBubblesKey(key: ByteArray): ByteArray = withContext(dispatcher) {
        randomKeyProvider().use { keyData ->
            crypto.encrypt(keyData, key, null)
        }.getOrElse {
            throw OSCryptoError.Code.BUBBLES_MASTER_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun reEncryptIndexKey(): ByteArray = withContext(dispatcher) {
        crypto.encrypt(searchIndexKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun reEncryptItemEditionKey(): ByteArray = withContext(dispatcher) {
        crypto.encrypt(itemEditionKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_EDITION_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    private suspend fun retrieveKeyForIndex(safeCrypto: SafeCrypto): Unit = withContext(dispatcher) {
        val encKey = safeCrypto.encIndexKey
        searchIndexKey = crypto.decrypt(encKey, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.INDEX_KEY_DECRYPTION_FAIL.get(cause = it)
        }
    }

    // TODO <multisafe> missing key should be considered as an error (no migration added to create it?)
    private suspend fun retrieveKeyForEdition(safeCrypto: SafeCrypto): Unit = withContext(dispatcher) {
        val encKey = safeCrypto.encItemEditionKey
        itemEditionKey = crypto.decrypt(encKey, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.ITEM_EDITION_KEY_DECRYPTION_FAIL.get(cause = it)
        }
    }

    // TODO <multisafe> missing key should be considered as an error (no migration added to create it?)
    private suspend fun retrieveKeyForBubblesContact(safeCrypto: SafeCrypto): Unit = withContext(dispatcher) {
        val encKey = safeCrypto.encBubblesKey
        if (encKey != null) {
            bubblesMasterKey = crypto.decrypt(encKey, masterKey!!, null).getOrElse {
                throw OSCryptoError.Code.BUBBLES_CONTACT_KEY_DECRYPTION_FAIL.get(cause = it)
            }
        } else {
            generateBubblesKey(masterKey!!)
        }
    }

    private suspend fun reEncryptBubblesContactKey(): ByteArray = withContext(dispatcher) {
        crypto.encrypt(bubblesMasterKey!!, masterKey!!, null).getOrElse {
            throw OSCryptoError.Code.BUBBLES_CONTACT_KEY_ENCRYPTION_FAIL.get(cause = it)
        }
    }

    override suspend fun enableBiometric(biometricCipher: Cipher, key: ByteArray?): BiometricCryptoMaterial = withContext(dispatcher) {
        try {
            biometricEngine.encryptKey(key ?: masterKey!!, biometricCipher)
        } catch (osError: OSCryptoError) {
            if (osError.code == OSCryptoError.Code.MASTER_KEY_NOT_LOADED) {
                biometricEngine.clear()
            }
            throw osError
        }
    }

    override suspend fun testCurrentPassword(password: CharArray): Unit = withContext(dispatcher) {
        val salt = safeRepository.getCurrentSalt()
        hashEngine.deriveKey(password, salt).use { testKey ->
            val isMasterKeyEquals = this@AndroidMainCryptoRepository.masterKey.contentEquals(testKey)
            if (!isMasterKeyEquals) {
                throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD)
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

    override suspend fun encryptRecentSearch(plainRecentSearch: List<String>): List<ByteArray> = withContext(dispatcher) {
        plainRecentSearch.map { element ->
            crypto.encrypt(plainData = element.encodeToByteArray(), key = searchIndexKey!!, associatedData = null).getOrElse {
                throw OSCryptoError.Code.RECENT_SEARCH_ENCRYPTION_FAIL.get(cause = it)
            }
        }
    }

    override suspend fun derivePassword(salt: ByteArray, password: CharArray): ByteArray {
        return hashEngine.deriveKey(password, salt)
    }

    override suspend fun decryptRecentSearch(encRecentSearch: List<ByteArray>): List<String> = withContext(dispatcher) {
        encRecentSearch.map { cypherData ->
            crypto.decrypt(cypherData, searchIndexKey!!, null).getOrElse {
                throw OSCryptoError.Code.RECENT_SEARCH_DECRYPTION_FAIL.get(cause = it)
            }.decodeToString()
        }
    }

    override suspend fun encryptIndexWord(words: List<String>): List<ByteArray> = withContext(dispatcher) {
        words.map { word ->
            crypto.encrypt(
                plainData = word.encodeToByteArray(),
                key = searchIndexKey!!,
                associatedData = null,
            ).getOrElse { err -> throw OSCryptoError.Code.INDEX_WORD_ENCRYPTION_FAIL.get(cause = err) }
        }
    }

    override suspend fun decryptIndexWord(encWords: List<ByteArray>): List<String> = withContext(dispatcher) {
        encWords.map { encWord ->
            crypto.decrypt(encWord, searchIndexKey!!, null).getOrElse {
                throw OSCryptoError.Code.INDEX_WORD_DECRYPTION_FAIL.get(cause = it)
            }.decodeToString()
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
}
