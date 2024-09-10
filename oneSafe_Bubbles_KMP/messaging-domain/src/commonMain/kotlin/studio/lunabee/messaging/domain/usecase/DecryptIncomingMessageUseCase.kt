/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/7/2023 - for the oneSafe6 SDK.
 * Last modified 6/7/23, 4:41 PM
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.bubbles.domain.model.EncryptEntry
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.bubbles.error.BubblesCryptoError
import studio.lunabee.bubbles.error.BubblesDomainError
import studio.lunabee.bubbles.error.BubblesError
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRPrivateKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DoubleRatchetError
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.MessageHeader
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.extension.asBubblesError
import studio.lunabee.messaging.domain.model.DecryptIncomingMessageData
import studio.lunabee.messaging.domain.model.OSEncryptedMessage
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.model.proto.ProtoHandShakeMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessageData
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository

/**
 * Try to decrypt a message with every contact key
 */
class DecryptIncomingMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val messagingCryptoRepository: MessagingCryptoRepository,
    private val getAllContactsUseCase: GetAllContactsUseCase,
    private val doubleRatchetEngine: DoubleRatchetEngine,
    private val handShakeDataRepository: HandShakeDataRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    private val contactRepository: ContactRepository,
    private val getHandShakeDataUseCase: GetHandShakeDataUseCase,
) {
    /**
     * @param messageData The raw message
     *
     * @return failure with NO_MATCHING_CONTACT error code if no contact shared key has been found to decrypt the message.
     */
    suspend operator fun invoke(messageData: ByteArray): LBResult<DecryptIncomingMessageData> {
        // We try to parse the data as HandShakeMessage to know what kind of message we are dealing with
        val handShakeMessage = tryParseHandShakeMessage(messageData)
        return getAllContactsUseCase().first()
            .asFlow()
            .map { contact ->
                val localKey = contactKeyRepository.getContactLocalKey(contact.id)
                if (handShakeMessage != null) {
                    handleHandShakeMessage(handShakeMessage, contact, localKey)
                } else {
                    handleMessage(messageData, contact, localKey)
                }
            }.firstOrNull { result ->
                // Get the first result different from BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY and return it
                val error = (result as? LBResult.Failure)?.throwable
                (error as? BubblesCryptoError)?.code != BubblesCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY &&
                    (error as? BubblesDomainError)?.code != BubblesDomainError.Code.WRONG_CONTACT
            } ?: LBResult.Failure(BubblesDomainError(BubblesDomainError.Code.NO_MATCHING_CONTACT))
    }

    private suspend fun handleHandShakeMessage(
        handShakeMessage: ProtoHandShakeMessage,
        contact: Contact,
        localKey: ContactLocalKey,
    ): LBResult<DecryptIncomingMessageData> = when {
        // If the sharedConversationId don't correspond to the proto conversationId, it means that its the wrong contact
        handShakeMessage.conversationId != contact.sharedConversationId.uuidString() -> LBResult.Failure(
            BubblesDomainError(BubblesDomainError.Code.WRONG_CONTACT),
        )
        // If recipientID equals the contact id, it mean that its a message we send that message and we can't decrypt it
        handShakeMessage.recipientId == contact.id.uuidString() -> LBResult.Success(
            DecryptIncomingMessageData.DecryptOwnMessage(contact.id),
        )
        else -> BubblesError.runCatching {
            decryptHandShake(handShakeMessage, contact, localKey)
        }
    }

    private suspend fun handleMessage(
        messageData: ByteArray,
        contact: Contact,
        localKey: ContactLocalKey,
    ): LBResult<DecryptIncomingMessageData> {
        val encSharedKey = contact.encSharedKey
        return if (encSharedKey != null) {
            BubblesError.runCatching { decryptMessage(messageData, contact, encSharedKey, localKey) }
        } else {
            LBResult.Failure(BubblesDomainError(BubblesDomainError.Code.WRONG_CONTACT))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun decryptHandShake(
        plainMessageProto: ProtoHandShakeMessage,
        contact: Contact,
        localKey: ContactLocalKey,
    ): DecryptIncomingMessageData {
        val plainMessage = OSEncryptedMessage(
            body = plainMessageProto.body,
            messageHeader = MessageHeader(
                messageNumber = plainMessageProto.header.messageNumber,
                sequenceNumber = plainMessageProto.header.sequenceMessageNumber,
                publicKey = DRPublicKey(plainMessageProto.header.publicKey),
            ),
            recipientId = DoubleRatchetUUID(plainMessageProto.recipientId),
        )
        // We skip the handshake step if we already did one
        if (contact.encSharedKey == null) {
            val privateKey = getHandShakeDataUseCase(contact.id).data?.oneSafePrivateKey!!
            val sharedSecret = doubleRatchetKeyRepository.createDiffieHellmanSharedSecret(
                DRPublicKey(plainMessageProto.oneSafePublicKey),
                DRPrivateKey(privateKey),
            )
            val encSharedKey = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(sharedSecret.value))
            contactRepository.addContactSharedKey(contact.id, ContactSharedKey(encSharedKey))

            // We remove handShake data since we don't need it anymore
            handShakeDataRepository.delete(contact.id)
        }
        val messageKey = try {
            doubleRatchetEngine.getReceiveKey(messageHeader = plainMessage.messageHeader, contact.id)
        } catch (e: DoubleRatchetError) {
            if (e.type == DoubleRatchetError.Type.MessageKeyNotFound) {
                return DecryptIncomingMessageData.AlreadyDecryptedMessage(contact.id)
            } else {
                throw e.asBubblesError()
            }
        }

        val plainBody = messagingCryptoRepository.decryptMessage(plainMessage.body, messageKey)
        val plainMessageDataProto = ProtoBuf.decodeFromByteArray<ProtoMessageData>(plainBody)

        // Don't return the message if it's an invitation message and you already received the handShake
        val osPlainMessage = if (plainMessageDataProto.content == MessagingConstant.FirstMessageData && contact.encSharedKey != null) {
            null
        } else {
            SharedMessage(
                content = plainMessageDataProto.content,
                sentAt = plainMessageDataProto.sentAt.toInstant(),
                recipientId = plainMessage.recipientId,
            )
        }
        return DecryptIncomingMessageData.NewMessage(contact.id, osPlainMessage, messageKey)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun decryptMessage(
        messageData: ByteArray,
        contact: Contact,
        sharedKey: ContactSharedKey,
        localKey: ContactLocalKey,
    ): DecryptIncomingMessageData {
        val plainData = bubblesCryptoRepository.sharedDecrypt(messageData, localKey, sharedKey)
        val plainMessageProto = ProtoBuf.decodeFromByteArray<ProtoMessage>(plainData)
        val plainMessage = OSEncryptedMessage(
            body = plainMessageProto.body,
            messageHeader = MessageHeader(
                messageNumber = plainMessageProto.header.messageNumber,
                sequenceNumber = plainMessageProto.header.sequenceMessageNumber,
                publicKey = DRPublicKey(plainMessageProto.header.publicKey),
            ),
            recipientId = DoubleRatchetUUID(plainMessageProto.recipientId),
        )
        if (plainMessage.messageHeader.messageNumber == 0) {
            handShakeDataRepository.delete(contact.id)
        }

        return when (plainMessage.recipientId) {
            contact.id -> DecryptIncomingMessageData.DecryptOwnMessage(contact.id)
            else -> {
                val messageKey = try {
                    doubleRatchetEngine.getReceiveKey(messageHeader = plainMessage.messageHeader, contact.id)
                } catch (e: DoubleRatchetError) {
                    if (e.type == DoubleRatchetError.Type.MessageKeyNotFound) {
                        return DecryptIncomingMessageData.AlreadyDecryptedMessage(contact.id)
                    } else {
                        throw e.asBubblesError()
                    }
                }
                val plainBody = messagingCryptoRepository.decryptMessage(plainMessage.body, messageKey)
                val plainMessageDataProto = ProtoBuf.decodeFromByteArray<ProtoMessageData>(plainBody)
                DecryptIncomingMessageData.NewMessage(
                    contactId = contact.id,
                    osPlainMessage = SharedMessage(
                        content = plainMessageDataProto.content,
                        recipientId = plainMessage.recipientId,
                        sentAt = plainMessageDataProto.sentAt.toInstant(),
                    ),
                    messageKey = messageKey,
                )
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun tryParseHandShakeMessage(messageData: ByteArray): ProtoHandShakeMessage? {
        return try {
            val result = ProtoBuf.decodeFromByteArray<ProtoHandShakeMessage>(messageData)
            return result
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
