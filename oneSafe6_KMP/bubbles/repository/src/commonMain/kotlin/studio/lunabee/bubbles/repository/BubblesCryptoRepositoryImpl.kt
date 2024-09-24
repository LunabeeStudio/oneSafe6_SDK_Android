/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/07/2024 10:08
 */

package studio.lunabee.bubbles.repository

import studio.lunabee.bubbles.domain.crypto.BubblesCryptoEngine
import studio.lunabee.bubbles.domain.crypto.BubblesDataHashEngine
import studio.lunabee.bubbles.domain.crypto.BubblesRandomKeyProvider
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.BubblesCryptoError
import studio.lunabee.onesafe.use

class BubblesCryptoRepositoryImpl @Inject constructor(
    private val mainCryptoRepository: BubblesMainCryptoRepository,
    private val crypto: BubblesCryptoEngine,
    private val randomKeyProvider: BubblesRandomKeyProvider,
    private val mapper: CryptoDataMapper,
    private val hkdfHashEngine: BubblesDataHashEngine,
) : BubblesCryptoRepository {

    override suspend fun generateLocalKeyForContact(): ContactLocalKey {
        return randomKeyProvider.invoke().use { plainKey ->
            val encKey = mainCryptoRepository.encryptBubbles(plainKey)
            ContactLocalKey(encKey)
        }
    }

    override suspend fun <Data : Any> localEncrypt(key: ContactLocalKey, encryptEntry: EncryptEntry<Data>): ByteArray {
        val data = encryptEntry.data
        val mapBlock = encryptEntry.mapBlock
        val rawData = mapper(mapBlock, data)
        return mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
            crypto.bubblesEncrypt(rawData, rawKey, null)
                ?: throw BubblesCryptoError(BubblesCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY)
        }
    }

    override suspend fun <Data : Any> localEncrypt(key: ContactLocalKey, encryptEntries: List<EncryptEntry<Data>?>): List<ByteArray?> {
        return mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
            encryptEntries.map { encryptEntry ->
                encryptEntry?.let {
                    val data = encryptEntry.data
                    val mapBlock = encryptEntry.mapBlock
                    val rawData = mapper(mapBlock, data)
                    crypto.bubblesEncrypt(rawData, rawKey, null)
                        ?: throw BubblesCryptoError(BubblesCryptoError.Code.ENCRYPTION_FAILED_BAD_KEY)
                }
            }
        }
    }

    override suspend fun <Data : Any> localDecrypt(key: ContactLocalKey, decryptEntry: DecryptEntry<Data>): Data {
        val rawData = mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
            crypto.bubblesDecrypt(decryptEntry.data, rawKey, null)
                ?: throw BubblesCryptoError(BubblesCryptoError.Code.DECRYPTION_FAILED_BAD_KEY)
        }
        return mapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
    }

    override suspend fun localDecrypt(key: ContactLocalKey, decryptEntries: List<DecryptEntry<out Any>?>): List<Any?> {
        val rawData = mainCryptoRepository.decryptBubbles(key.encKey).use { rawKey ->
            decryptEntries.map {
                it to it?.let {
                    crypto.bubblesDecrypt(it.data, rawKey, null)
                        ?: throw BubblesCryptoError(BubblesCryptoError.Code.DECRYPTION_FAILED_BAD_KEY)
                }
            }
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
        data: ByteArray,
        localKey: ContactLocalKey,
        sharedKey: ContactSharedKey,
    ): ByteArray {
        val plainSharedKey = localDecrypt(localKey, DecryptEntry(sharedKey.encKey, ByteArray::class))
        return crypto.bubblesEncrypt(data, plainSharedKey, null)
            ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY)
    }

    override suspend fun sharedDecrypt(
        data: ByteArray,
        localKey: ContactLocalKey,
        sharedKey: ContactSharedKey,
    ): ByteArray {
        val plainSharedKey = localDecrypt(localKey, DecryptEntry(sharedKey.encKey, ByteArray::class))
        return crypto.bubblesDecrypt(data, plainSharedKey, null)
            ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY)
    }

    override suspend fun deriveUUIDToKey(uuid: DoubleRatchetUUID, keyLength: Int): ByteArray {
        return hkdfHashEngine.deriveKey(
            uuid.toByteArray(),
            uuid.toByteArray(),
            keyLength,
        )
    }
}
