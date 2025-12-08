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
import studio.lunabee.onesafe.domain.model.crypto.NewSafeCrypto
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.crypto.Cipher
import kotlin.reflect.KClass
import kotlin.time.Duration

interface MainCryptoRepository {
    suspend fun <Data : Any> decrypt(key: SafeItemKey, decryptEntry: DecryptEntry<Data>): Data

    suspend fun decrypt(key: SafeItemKey, decryptEntries: Collection<DecryptEntry<out Any>?>): List<Any?>

    suspend fun <Data : Any?, Output : Any> decryptWithData(
        key: SafeItemKey,
        decryptEntries: List<Pair<Data, DecryptEntry<out Output>?>>,
    ): List<Pair<Data, Output?>>

    suspend fun <Data : Any> decrypt(file: File, key: SafeItemKey, clazz: KClass<Data>, mapper: (ByteArray.() -> Data)? = null): Data

    suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntry: EncryptEntry<Data>): ByteArray

    suspend fun <Data : Any> encrypt(key: SafeItemKey, encryptEntries: List<EncryptEntry<Data>?>): List<ByteArray?>

    suspend fun getDecryptStream(cipherFile: File, key: SafeItemKey): InputStream

    suspend fun getEncryptStream(cipherFile: File, key: SafeItemKey): OutputStream

    suspend fun getFileEditionEncryptStream(plainFile: File): OutputStream

    suspend fun getFileEditionDecryptStream(encFile: File): InputStream

    suspend fun encrypt(outputStream: OutputStream, key: ByteArray): OutputStream

    suspend fun encryptIndexWord(words: List<String>): List<ByteArray>

    suspend fun decryptIndexWord(encWords: List<ByteArray>): List<String>

    suspend fun generateKeyForItemId(itemId: UUID): SafeItemKey

    suspend fun importItemKey(rawKeyValue: ByteArray, keyId: UUID): SafeItemKey

    suspend fun unloadMasterKeys()

    /**
     * Generate cryptographic keys from a master key. Does not load them.
     */
    suspend fun generateCrypto(key: ByteArray, salt: ByteArray, biometricCipher: Cipher?): NewSafeCrypto

    /**
     * Re-generate cryptographic keys from a master key. Override the loaded masker key (loaded master key mandatory)
     */
    suspend fun regenerateAndOverrideLoadedCrypto(key: ByteArray, salt: ByteArray, biometricCipher: Cipher?): NewSafeCrypto

    /**
     * Test a password against the current loaded master key and salt
     */
    suspend fun testCurrentPassword(password: CharArray)

    suspend fun enableBiometric(biometricCipher: Cipher, key: ByteArray? = null): BiometricCryptoMaterial

    suspend fun reEncryptItemKey(itemKey: SafeItemKey, key: ByteArray)

    fun isCryptoDataInMemoryFlow(): Flow<Boolean>

    suspend fun loadMasterKey(masterKey: ByteArray)

    suspend fun decryptRecentSearch(encRecentSearch: List<ByteArray>): List<String>

    suspend fun encryptRecentSearch(plainRecentSearch: String): ByteArray

    suspend fun derivePassword(salt: ByteArray, password: CharArray): ByteArray

    /**
     * Returns true if the cryptographic keys are loaded
     *
     * @param timeout Optional duration to wait before returning false. Use [Duration.ZERO] or negative value to return immediately and
     * [Duration.INFINITE] to wait indefinitely.
     */
    suspend fun isCryptoDataInMemory(timeout: Duration): Boolean

    companion object {
        // TODO <multisafe> move somewhere
        const val MasterKeyTestValue: String = "44c5dac9-17ba-4690-9275-c7471b2e0582"
    }
}
