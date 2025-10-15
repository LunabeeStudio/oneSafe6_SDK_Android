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
 * Created by Lunabee Studio / Date - 7/25/2024 - for the oneSafe6 SDK.
 * Last modified 25/07/2024 16:41
 */

package studio.lunabee.messaging.repository

import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.doubleratchet.model.AsymmetricKeyPair
import studio.lunabee.doubleratchet.model.Conversation
import studio.lunabee.doubleratchet.model.DRChainKey
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DRMessageKeyId
import studio.lunabee.doubleratchet.model.DRPrivateKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DRRootKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.messaging.domain.model.EncConversation
import studio.lunabee.messaging.domain.model.EncDoubleRatchetKey
import studio.lunabee.messaging.repository.datasource.ConversationLocalDatasource
import studio.lunabee.messaging.repository.datasource.DoubleRatchetKeyLocalDatasource
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry

// TODO rework KMP lib to have use case like GetConversationUseCase
class DoubleRatchetDatasourceImpl @Inject constructor(
    private val conversationLocalDatasource: ConversationLocalDatasource,
    private val doubleRatchetKeyLocalDatasource: DoubleRatchetKeyLocalDatasource,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
) : DoubleRatchetLocalDatasource {

    override suspend fun getConversation(id: DoubleRatchetUUID): Conversation? {
        val key = contactKeyRepository.getContactLocalKey(id)
        return conversationLocalDatasource.getById(id)?.let { encConversation ->
            val decryptEntries = listOf(
                DecryptEntry(encConversation.encPersonalPublicKey, ByteArray::class), // +0
                DecryptEntry(encConversation.encPersonalPrivateKey, ByteArray::class), // +1
                DecryptEntry(encConversation.encMessageNumber, Int::class), // +2
                DecryptEntry(encConversation.encSequenceNumber, Int::class), // +3
                encConversation.encRootKey?.let { DecryptEntry(it, ByteArray::class) }, // +4
                encConversation.encSendingChainKey?.let { DecryptEntry(it, ByteArray::class) }, // +5
                encConversation.encReceiveChainKey?.let { DecryptEntry(it, ByteArray::class) }, // +6
                encConversation.encLastContactPublicKey?.let { DecryptEntry(it, ByteArray::class) }, // +7
                encConversation.encReceivedLastMessageNumber?.let { DecryptEntry(it, Int::class) }, // +8
            )
            val plainEntries = bubblesCryptoRepository.localDecrypt(key, decryptEntries)
            Conversation(
                id = id,
                personalKeyPair = AsymmetricKeyPair(
                    publicKey = DRPublicKey(plainEntries[0] as ByteArray),
                    privateKey = DRPrivateKey(plainEntries[1] as ByteArray),
                ),
                messageNumber = plainEntries[2] as Int,
                sequenceNumber = plainEntries[3] as Int,
                rootKey = (plainEntries[4] as? ByteArray)?.let { DRRootKey(it) },
                sendingChainKey = (plainEntries[5] as? ByteArray)?.let { DRChainKey(it) },
                receiveChainKey = (plainEntries[6] as? ByteArray)?.let { DRChainKey(it) },
                lastContactPublicKey = (plainEntries[7] as? ByteArray)?.let { DRPublicKey(it) },
                receivedLastMessageNumber = plainEntries[8] as? Int,
            )
        }
    }

    override suspend fun popMessageKey(id: DRMessageKeyId): DRMessageKey? {
        val key = doubleRatchetKeyLocalDatasource.getById(id.value)?.let { encData ->
            val key = contactKeyRepository.getContactLocalKey(DoubleRatchetUUID.fromString(id.conversationId))
            DRMessageKey(bubblesCryptoRepository.localDecrypt(key, DecryptEntry(encData, ByteArray::class)))
        }
        doubleRatchetKeyLocalDatasource.deleteById(id.value)
        return key
    }

    override suspend fun saveMessageKey(id: DRMessageKeyId, key: DRMessageKey) {
        val contactKey = contactKeyRepository.getContactLocalKey(DoubleRatchetUUID.fromString(id.conversationId))
        val encDoubleRatchetKey = EncDoubleRatchetKey(
            id = id.value,
            data = bubblesCryptoRepository.localEncrypt(contactKey, EncryptEntry(key.value)),
        )
        return doubleRatchetKeyLocalDatasource.insert(encDoubleRatchetKey)
    }

    override suspend fun saveOrUpdateConversation(conversation: Conversation) {
        val key = contactKeyRepository.getContactLocalKey(conversation.id)
        val encryptEntries = listOf<EncryptEntry<Any>?>(
            EncryptEntry(conversation.personalKeyPair.publicKey.value), // +0
            EncryptEntry(conversation.personalKeyPair.privateKey.value), // +1
            EncryptEntry(conversation.nextMessageNumber), // +2
            EncryptEntry(conversation.nextSequenceNumber), // +3
            conversation.rootKey?.value?.let { EncryptEntry(it) }, // +4
            conversation.sendingChainKey?.value?.let { EncryptEntry(it) }, // +5
            conversation.receiveChainKey?.value?.let { EncryptEntry(it) }, // +6
            conversation.lastContactPublicKey?.value?.let { EncryptEntry(it) }, // +7
            conversation.receivedLastMessageNumber?.let { EncryptEntry(it) }, // +8
        )
        val encEntries: List<ByteArray?> = bubblesCryptoRepository.localEncrypt(key, encryptEntries)
        val encConversation = EncConversation(
            id = conversation.id,
            encPersonalPublicKey = encEntries[0]!!,
            encPersonalPrivateKey = encEntries[1]!!,
            encMessageNumber = encEntries[2]!!,
            encSequenceNumber = encEntries[3]!!,
            encRootKey = encEntries[4],
            encSendingChainKey = encEntries[5],
            encReceiveChainKey = encEntries[6],
            encLastContactPublicKey = encEntries[7],
            encReceivedLastMessageNumber = encEntries[8],
        )
        return conversationLocalDatasource.insert(encConversation)
    }
}
