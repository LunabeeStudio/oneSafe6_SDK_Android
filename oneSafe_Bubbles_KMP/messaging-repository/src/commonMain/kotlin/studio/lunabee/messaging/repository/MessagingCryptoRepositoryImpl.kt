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
 * Last modified 12/07/2024 08:59
 */

package studio.lunabee.messaging.repository

import studio.lunabee.bubbles.domain.BubblesCryptoDataMapper
import studio.lunabee.bubbles.domain.crypto.BubblesCryptoEngine
import studio.lunabee.bubbles.domain.crypto.BubblesRandomKeyProvider
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.bubbles.domain.model.DecryptEntry
import studio.lunabee.bubbles.domain.model.EncryptEntry
import studio.lunabee.bubbles.domain.utils.use
import studio.lunabee.bubbles.error.BubblesCryptoError
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.messaging.repository.datasource.MessageQueueLocalDatasource

class MessagingCryptoRepositoryImpl @Inject constructor(
    private val bubblesCryptoEngine: BubblesCryptoEngine,
    private val randomKeyProvider: BubblesRandomKeyProvider,
    private val bubblesCryptoDataMapper: BubblesCryptoDataMapper,
    private val messageQueueLocalDatasource: MessageQueueLocalDatasource,
) : MessagingCryptoRepository {
    override suspend fun <Data : Any> queueEncrypt(encryptEntry: EncryptEntry<Data>): ByteArray {
        return getOrCreateQueueKey().use { key ->
            val data = encryptEntry.data
            val mapBlock = encryptEntry.mapBlock
            val rawData = bubblesCryptoDataMapper(mapBlock, data)
            bubblesCryptoEngine.bubblesEncrypt(rawData, key, null)
                ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_QUEUE_KEY)
        }
    }

    override suspend fun <Data : Any> queueDecrypt(decryptEntry: DecryptEntry<Data>): Data {
        return getOrCreateQueueKey().use { key ->
            val rawData = bubblesCryptoEngine.bubblesDecrypt(decryptEntry.data, key, null)
                ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_QUEUE_KEY)
            bubblesCryptoDataMapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
        }
    }

    override suspend fun decryptMessage(data: ByteArray, key: DRMessageKey): ByteArray {
        return bubblesCryptoEngine.bubblesDecrypt(data, key.value, null)
            ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_MESSAGE_KEY)
    }

    override suspend fun encryptMessage(data: ByteArray, key: DRMessageKey): ByteArray {
        return bubblesCryptoEngine.bubblesEncrypt(data, key.value, null)
            ?: throw BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY)
    }

    private suspend fun getOrCreateQueueKey(): ByteArray {
        var key = messageQueueLocalDatasource.retrieveValue(QUEUE_KEY_ALIAS)
        if (key == null) {
            key = randomKeyProvider.invoke()
            messageQueueLocalDatasource.insertValue(QUEUE_KEY_ALIAS, key)
        }
        return key
    }

    companion object {
        private const val QUEUE_KEY_ALIAS = "bfe3f6a9-918a-4bf6-af38-786679fa0c82"
    }
}
