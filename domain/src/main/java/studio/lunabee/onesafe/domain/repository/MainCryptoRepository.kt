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

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.crypto.Cipher
import kotlin.reflect.KClass

interface MainCryptoRepository {
    suspend fun <Data : Any> decrypt(key: SafeItemKey, decryptEntry: DecryptEntry<Data>): Data
    suspend fun decrypt(key: SafeItemKey, decryptEntries: List<DecryptEntry<out Any>?>): List<Any?>

    suspend fun <Data : Any?, Output : Any> decryptWithData(
        key: SafeItemKey,
        decryptEntries: List<Pair<Data, DecryptEntry<out Output>?>>,
    ): List<Pair<Data, Output?>>

    suspend fun <Data : Any> decrypt(file: File, key: SafeItemKey, clazz: KClass<Data>, mapper: (ByteArray.() -> Data)? = null): Data
    suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntry: EncryptEntry<Data>): ByteArray
    suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntries: List<EncryptEntry<Data>?>): List<ByteArray?>
    suspend fun getDecryptStream(cipherFile: File, key: SafeItemKey): InputStream
    suspend fun getEncryptStream(cipherFile: File, key: SafeItemKey): OutputStream
    fun getFileEditionEncryptStream(plainFile: File): OutputStream
    fun getFileEditionDecryptStream(encFile: File): InputStream
    suspend fun encrypt(outputStream: OutputStream, key: ByteArray): OutputStream
    suspend fun encryptIndexWord(indexWordEntry: List<PlainIndexWordEntry>): List<IndexWordEntry>
    suspend fun decryptIndexWord(encIndexWordEntry: List<IndexWordEntry>): List<PlainIndexWordEntry>
    suspend fun generateKeyForItemId(itemId: UUID): SafeItemKey
    suspend fun importItemKey(rawKeyValue: ByteArray, keyId: UUID): SafeItemKey
    suspend fun resetCryptography()
    fun unloadMasterKeys()
    fun hasMasterSalt(): Boolean
    fun getCurrentSalt(): ByteArray
    suspend fun loadMasterKeyFromPassword(password: CharArray)

    /**
     * Set the master key and salt. Fails if already set.
     */
    suspend fun storeMasterKeyAndSalt(key: ByteArray, salt: ByteArray)

    /**
     * Same as [storeMasterKeyAndSalt] but allow overrides of the current master key & salt.
     */
    suspend fun overrideMasterKeyAndSalt(key: ByteArray, salt: ByteArray)
    suspend fun testPassword(password: CharArray): Boolean

    suspend fun loadMasterKeyFromBiometric(cipher: Cipher)
    suspend fun retrieveMasterKeyFromBiometric(cipher: Cipher): ByteArray
    fun getCipherForBiometricForVerify(): Cipher
    fun getCipherForBiometricForCreate(): Cipher
    fun isBiometricEnabledFlow(): Flow<Boolean>
    fun disableBiometric()
    fun enableBiometric(biometricCipher: Cipher)
    suspend fun reEncryptItemKey(itemKey: SafeItemKey, key: ByteArray)
    fun isCryptoDataInMemoryFlow(): Flow<Boolean>
    suspend fun loadMasterKeyExternal(masterKey: ByteArray)
    suspend fun decryptRecentSearch(encRecentSearch: List<ByteArray>): List<String>
    suspend fun encryptRecentSearch(plainRecentSearch: List<String>): List<ByteArray>
    suspend fun generateBubblesKey()
    suspend fun deleteBubblesCrypto()

    /**
     * Encrypt [data] with the bubbles master key
     */
    suspend fun encryptBubbles(data: ByteArray): ByteArray

    /**
     * Decrypt [data] with the bubbles master key
     */
    suspend fun decryptBubbles(data: ByteArray): ByteArray
    fun isCryptoDataInMemory(): Boolean
}
