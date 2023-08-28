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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 3:34 PM
 */

package studio.lunabee.onesafe.bubbles.crypto

import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.cryptography.RandomKeyProvider
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.toByteArray
import studio.lunabee.onesafe.use
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.util.UUID
import javax.inject.Inject

class AndroidBubblesCryptoRepository @Inject constructor(
    private val mainCryptoRepository: MainCryptoRepository,
    private val crypto: CryptoEngine,
    private val randomKeyProvider: RandomKeyProvider,
    private val mapper: CryptoDataMapper,
    private val hkdfHashEngine: DataHashEngine,
) : BubblesCryptoRepository {

    override suspend fun generateLocalKeyForContact(): ContactLocalKey {
        return randomKeyProvider().use { plainKey ->
            val encKey = mainCryptoRepository.encryptBubbles(plainKey)
            ContactLocalKey(encKey)
        }
    }

    override suspend fun <Data : Any> localEncrypt(key: ContactLocalKey, encryptEntry: EncryptEntry<Data>): ByteArray {
        val data = encryptEntry.data
        val mapBlock = encryptEntry.mapBlock
        val rawData = mapper(mapBlock, data)
        return try {
            mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
                crypto.encrypt(rawData, rawKey, null)
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
        }
    }

    override suspend fun <Data : Any> localEncrypt(key: ContactLocalKey, encryptEntries: List<EncryptEntry<Data>?>): List<ByteArray?> {
        return mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
            encryptEntries.map { encryptEntry ->
                encryptEntry?.let {
                    val data = encryptEntry.data
                    val mapBlock = encryptEntry.mapBlock
                    val rawData = mapper(mapBlock, data)
                    try {
                        crypto.encrypt(rawData, rawKey, null)
                    } catch (e: GeneralSecurityException) {
                        throw OSCryptoError(OSCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY, cause = e)
                    }
                }
            }
        }
    }

    override suspend fun <Data : Any> localDecrypt(key: ContactLocalKey, decryptEntry: DecryptEntry<Data>): Data {
        val rawData = try {
            mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
                crypto.decrypt(decryptEntry.data, rawKey, null)
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }

        return mapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
    }

    override suspend fun localDecrypt(key: ContactLocalKey, decryptEntries: List<DecryptEntry<out Any>?>): List<Any?> {
        val rawData = try {
            mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
                decryptEntries.map { it to it?.let { crypto.decrypt(it.data, rawKey, null) } }
            }
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError(OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY, cause = e)
        }

        return rawData.map { entry ->
            entry.first?.let { key ->
                entry.second?.let { value ->
                    mapper(key.mapBlock, value, key.clazz)
                }
            }
        }
    }

    override suspend fun sharedEncrypt(
        outputStream: OutputStream,
        localKey: ContactLocalKey,
        sharedKey: ContactSharedKey,
    ): OutputStream = try {
        val plainSharedKey = localDecrypt(localKey, DecryptEntry(sharedKey.encKey, ByteArray::class))
        crypto.getCipherOutputStream(outputStream, plainSharedKey, null)
    } catch (e: GeneralSecurityException) {
        throw OSCryptoError(OSCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY, cause = e)
    }

    override suspend fun sharedDecrypt(
        data: ByteArray,
        localKey: ContactLocalKey,
        sharedKey: ContactSharedKey,
    ): ByteArray = try {
        val plainSharedKey = localDecrypt(localKey, DecryptEntry(sharedKey.encKey, ByteArray::class))
        crypto.decrypt(data, plainSharedKey, null)
    } catch (e: GeneralSecurityException) {
        throw OSCryptoError(OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY, cause = e)
    }

    override suspend fun deriveUUIDToKey(uuid: UUID, keyLength: Int): ByteArray {
        return hkdfHashEngine.deriveKey(uuid.toByteArray(), uuid.toByteArray(), ByteArray(keyLength))
    }
}
