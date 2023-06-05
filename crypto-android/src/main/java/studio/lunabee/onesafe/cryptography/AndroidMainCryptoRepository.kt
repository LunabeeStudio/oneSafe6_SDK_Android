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
import com.lunabee.lblogger.v
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.utils.SafeDataMutableStateFlow
import studio.lunabee.onesafe.cryptography.utils.dataStoreValueDelegate
import studio.lunabee.onesafe.cryptography.utils.safeCryptoArrayDelete
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.use
import java.io.File
import java.security.GeneralSecurityException
import java.util.UUID
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.reflect.KClass

private val log = LBLogger.get<AndroidMainCryptoRepository>()

@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
class AndroidMainCryptoRepository @Inject constructor(
    private val crypto: CryptoEngine,
    private val hashEngine: HashEngine,
    private val biometricEngine: BiometricEngine,
    @DatastoreEngineProvider(DataStoreType.Plain) private val dataStoreEngine: DatastoreEngine,
    private val featureFlags: FeatureFlags,
    private val itemKeyProvider: ItemKeyProvider,
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

    private val bubblesContactKeyFlow: SafeDataMutableStateFlow = SafeDataMutableStateFlow(
        overrideCode = OSCryptoError.Code.BUBBLES_CONTACT_KEY_ALREADY_LOADED,
        nullableCode = OSCryptoError.Code.BUBBLES_CONTACT_KEY_NOT_LOADED,
    )
    private var bubblesContactKey: ByteArray? by safeCryptoArrayDelete(bubblesContactKeyFlow)

    override fun isCryptoDataInMemory(): Flow<Boolean> {
        val flows = mutableListOf(masterKeyFlow, searchIndexKeyFlow)
        if (featureFlags.bubbles()) {
            flows += bubblesContactKeyFlow
        }
        return flows.merge().map { masterKey ->
            masterKey != null
        }.distinctUntilChanged()
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

    private var bubblesContactKeyDataStore: ByteArray? by dataStoreValueDelegate(
        key = DATASTORE_BUBBLES_CONTACT_KEY,
        datastoreEngine = dataStoreEngine,
        errorCodeIfOverrideExistingValue = OSCryptoError.Code.BUBBLES_CONTACT_KEY_ALREADY_GENERATED,
    )

    override fun hasMasterSalt(): Boolean = saltDataStore != null

    override fun getCurrentSalt(): ByteArray = saltDataStore!!

    override suspend fun resetCryptography() {
        dataStoreEngine.clearDataStore()
        unloadCryptographyKeys()
    }

    override fun unloadCryptographyKeys() {
        masterKey = null
        searchIndexKey = null
        if (featureFlags.bubbles()) {
            bubblesContactKey = null
        }
        log.v("cryptographic keys unloaded")
    }

    override suspend fun storeMasterKeyAndSalt(key: ByteArray, salt: ByteArray) {
        if (saltDataStore != null) {
            throw OSCryptoError(OSCryptoError.Code.MASTER_SALT_ALREADY_LOADED)
        }
        saltDataStore = salt.copyOf()
        masterKey = key.copyOf()
        log.v("cryptographic keys stored")

        masterKeyTestDataStore = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = masterKey!!,
            associatedData = null,
        )
        generateIndexKey()
        if (featureFlags.bubbles()) {
            generateBubblesContactKey()
        }
    }

    override suspend fun overrideMasterKeyAndSalt(key: ByteArray, salt: ByteArray) {
        key.copyInto(masterKey!!)

        dataStoreEngine.editValue(value = salt, key = DATASTORE_MASTER_SALT)
        val masterKeyTestValue = crypto.encrypt(
            plainData = MASTER_KEY_TEST_VALUE.encodeToByteArray(),
            key = masterKey!!,
            associatedData = null,
        )
        dataStoreEngine.editValue(value = masterKeyTestValue, key = DATASTORE_MASTER_KEY_TEST)
        reEncryptIndexKey()
        if (featureFlags.bubbles()) {
            reEncryptBubblesContactKey()
        }
    }

    override suspend fun loadMasterKeyFromBiometric(cipher: Cipher) {
        masterKey = biometricEngine.retrieveKey(cipher)
        retrieveKeyForIndex()
        if (featureFlags.bubbles()) {
            retrieveKeyForBubblesContact()
        }
        log.v("cryptographic keys loaded using biometric")
    }

    override suspend fun loadMasterKeyExternal(masterKey: ByteArray) {
        this.masterKey = masterKey.copyOf()
        retrieveKeyForIndex()
        if (featureFlags.bubbles()) {
            retrieveKeyForBubblesContact()
        }
        log.v("cryptographic keys externally loaded")
    }

    private suspend fun generateIndexKey() {
        searchIndexKeyDataStore = itemKeyProvider().use { keyData ->
            searchIndexKey = keyData.copyOf()
            crypto.encrypt(keyData, masterKey!!, null)
        }
    }

    override suspend fun generateBubblesContactKey() {
        bubblesContactKeyDataStore = itemKeyProvider().use { keyData ->
            bubblesContactKey = keyData.copyOf()
            crypto.encrypt(keyData, masterKey!!, null)
        }
    }

    private suspend fun reEncryptIndexKey() {
        val key = crypto.encrypt(searchIndexKey!!, masterKey!!, null)
        dataStoreEngine.editValue(value = key, key = DATASTORE_SEARCH_INDEX_KEY)
    }

    private suspend fun retrieveKeyForIndex() {
        searchIndexKey = crypto.decrypt(searchIndexKeyDataStore!!, masterKey!!, null)
    }

    private suspend fun retrieveKeyForBubblesContact() {
        if (bubblesContactKeyDataStore != null) {
            bubblesContactKey = crypto.decrypt(bubblesContactKeyDataStore!!, masterKey!!, null)
        } else {
            generateBubblesContactKey()
        }
    }

    private suspend fun reEncryptBubblesContactKey() {
        val key = crypto.encrypt(bubblesContactKey!!, masterKey!!, null)
        dataStoreEngine.editValue(value = key, key = DATASTORE_BUBBLES_CONTACT_KEY)
    }

    override fun getCipherForBiometricForVerify(): Cipher = biometricEngine.getCipherBiometricForDecrypt()

    override fun getCipherForBiometricForCreate(): Cipher = biometricEngine.createCipherBiometricForEncrypt()
    override fun isBiometricEnabledFlow(): Flow<Boolean> = biometricEngine.isBiometricEnabledFlow()
    override fun disableBiometric(): Unit = biometricEngine.disableBiometric()
    override fun enableBiometric(biometricCipher: Cipher) {
        try {
            biometricEngine.storeKey(masterKey!!, biometricCipher)
        } catch (osError: OSCryptoError) {
            if (osError.code == OSCryptoError.Code.MASTER_KEY_NOT_LOADED) {
                biometricEngine.disableBiometric()
                throw osError
            }
        }
    }

    override suspend fun loadMasterKeyFromPassword(password: CharArray) {
        password.use {
            retrieveMasterKeyFromPassword(password)?.let {
                masterKey = it
                retrieveKeyForIndex()
                if (featureFlags.bubbles()) {
                    retrieveKeyForBubblesContact()
                }
                log.v("cryptographic keys loaded using password")
            }
        }
    }

    override suspend fun testPassword(password: CharArray): Boolean {
        return retrieveMasterKeyFromPassword(password)?.randomize() != null
    }

    private suspend fun retrieveMasterKeyFromPassword(password: CharArray): ByteArray? {
        return password.use {
            val salt = saltDataStore ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
            salt.use {
                val encMasterKeyTest = masterKeyTestDataStore ?: throw OSCryptoError(OSCryptoError.Code.MASTER_KEY_NOT_GENERATED)
                hashEngine.deriveKey(password, salt).use { masterKey ->
                    try {
                        val plainMasterKeyTest = crypto.decrypt(encMasterKeyTest, masterKey, null).decodeToString()
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

    override suspend fun generateKeyForItemId(itemId: UUID): SafeItemKey {
        return itemKeyProvider().use { keyData ->
            val encryptedKey = crypto.encrypt(keyData, masterKey!!, null)
            SafeItemKey(itemId, encryptedKey)
        }
    }

    override suspend fun importItemKey(rawKeyValue: ByteArray, keyId: UUID): SafeItemKey {
        val encKeyValue = crypto.encrypt(rawKeyValue, masterKey!!, null)
        return SafeItemKey(id = keyId, encValue = encKeyValue)
    }

    override suspend fun <Data : Any> decrypt(key: SafeItemKey, decryptEntry: DecryptEntry<Data>): Data {
        return doDecrypt(
            key = key,
            decrypt = { rawKey ->
                crypto.decrypt(decryptEntry.data, rawKey, null)
            },
            clazz = decryptEntry.clazz,
            mapper = decryptEntry.mapper,
        )
    }

    override suspend fun decrypt(key: SafeItemKey, decryptEntries: List<DecryptEntry<out Any>?>): List<Any?> {
        return try {
            crypto.decrypt(key.encValue, masterKey!!, null).use { rawKey ->
                decryptEntries.map { decryptEntry ->
                    if (decryptEntry == null) {
                        null
                    } else {
                        val rawData = crypto.decrypt(decryptEntry.data, rawKey, null)
                        mapClearData(decryptEntry.mapper, rawData, decryptEntry.clazz)
                    }
                }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any> decrypt(file: File, key: SafeItemKey, clazz: KClass<Data>, mapper: (ByteArray.() -> Data)?): Data {
        val aFile = AtomicFile(file)
        return doDecrypt(
            key = key,
            decrypt = { rawKey ->
                crypto.decrypt(aFile, rawKey, null)
            },
            clazz = clazz,
            mapper = mapper,
        )
    }

    private suspend fun <Data : Any> doDecrypt(
        key: SafeItemKey,
        decrypt: suspend (rawKey: ByteArray) -> ByteArray,
        clazz: KClass<Data>,
        mapper: (ByteArray.() -> Data)?,
    ): Data {
        val rawData = try {
            crypto.decrypt(key.encValue, masterKey!!, null).use { rawKey ->
                decrypt(rawKey)
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }

        return mapClearData(mapper, rawData, clazz)
    }

    private fun <Data : Any> mapClearData(
        mapper: (ByteArray.() -> Data)?,
        rawData: ByteArray,
        clazz: KClass<out Data>,
    ): Data {
        @Suppress("UNCHECKED_CAST")
        return when {
            mapper != null -> rawData.mapper()
            clazz == String::class -> rawData.decodeToString() as Data
            clazz == Int::class -> rawData.toInt() as Data
            clazz == SafeItemFieldKind::class -> rawData.toSafeItemFieldKind() as Data
            clazz == UUID::class -> rawData.toUUID() as Data
            clazz == ByteArray::class -> rawData as Data
            else -> throw OSCryptoError(
                OSCryptoError.Code.MISSING_MAPPER,
                "No mapper found for type ${clazz.simpleName}",
            )
        }
    }

    override suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntry: EncryptEntry<Data>): ByteArray {
        val data = encryptEntry.data
        val mapper = encryptEntry.mapper

        val rawData = mapToData(mapper, data)

        return try {
            crypto.decrypt(key.encValue, masterKey!!, null).use { rawKey ->
                crypto.encrypt(rawData, rawKey, null)
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntries: List<EncryptEntry<Data>?>): List<ByteArray?> {
        val rawDataList = encryptEntries.map { entry ->
            entry?.let { encryptEntry ->
                mapToData(encryptEntry.mapper, encryptEntry.data)
            }
        }

        return try {
            crypto.decrypt(key.encValue, masterKey!!, null).use { rawKey ->
                rawDataList.map { data ->
                    data?.let { crypto.encrypt(it, rawKey, null) }
                }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun encryptIndexWord(indexWordEntry: List<PlainIndexWordEntry>): List<IndexWordEntry> {
        return indexWordEntry.map {
            IndexWordEntry(
                crypto.encrypt(plainData = it.word.encodeToByteArray(), key = searchIndexKey!!, associatedData = null),
                it.itemMatch,
                it.fieldMatch,
            )
        }
    }

    override suspend fun encryptRecentSearch(plainRecentSearch: List<String>): List<ByteArray> {
        return plainRecentSearch.map { _element ->
            crypto.encrypt(plainData = _element.encodeToByteArray(), key = searchIndexKey!!, associatedData = null)
        }
    }

    override suspend fun decryptRecentSearch(encRecentSearch: List<ByteArray>): List<String> {
        return encRecentSearch.map { _cypherData ->
            crypto.decrypt(_cypherData, searchIndexKey!!, null).decodeToString()
        }
    }

    override suspend fun decryptIndexWord(encIndexWordEntry: List<IndexWordEntry>): List<PlainIndexWordEntry> {
        return encIndexWordEntry.map {
            PlainIndexWordEntry(
                crypto.decrypt(it.encWord, searchIndexKey!!, null).decodeToString(),
                it.itemMatch,
                it.fieldMatch,
            )
        }
    }

    override suspend fun reEncryptItemKey(itemKey: SafeItemKey, key: ByteArray) {
        crypto.decrypt(cipherData = itemKey.encValue, key = masterKey!!, associatedData = null).use { plainKey ->
            crypto.encrypt(plainKey, key, null).copyInto(itemKey.encValue)
        }
    }

    override suspend fun encryptForBubblesContact(data: ByteArray): ByteArray =
        crypto.encrypt(data, bubblesContactKey!!, null)

    override suspend fun decryptForBubblesContact(data: ByteArray): String {
        return crypto.decrypt(data, bubblesContactKey!!, null).decodeToString()
    }

    private fun <Data : Any> mapToData(mapper: ((Data) -> ByteArray)?, data: Data) = when {
        mapper != null -> mapper(data)
        data is String -> data.encodeToByteArray()
        data is Int -> data.toByteArray()
        data is SafeItemFieldKind -> data.toByteArray()
        data is UUID -> data.toByteArray()
        data is ByteArray -> data
        else -> throw OSCryptoError(
            OSCryptoError.Code.MISSING_MAPPER,
            "No mapper found or provided for type ${data::class.simpleName}",
        )
    }

    companion object {
        private const val DATASTORE_SEARCH_INDEX_KEY = "f0ab7671-5314-41dc-9f57-3c689180ab33"
        private const val DATASTORE_MASTER_SALT = "b282a019-4337-45a3-8bf6-da657ad39a6c"
        private const val DATASTORE_MASTER_KEY_TEST = "f9e3fa44-2f54-4246-8ba6-2784a18b63ea"
        private const val DATASTORE_BUBBLES_CONTACT_KEY: String = "2b96478c-cbd4-4150-b591-6fe5a4dffc5f"

        private const val MASTER_KEY_TEST_VALUE = "44c5dac9-17ba-4690-9275-c7471b2e0582"
    }
}
