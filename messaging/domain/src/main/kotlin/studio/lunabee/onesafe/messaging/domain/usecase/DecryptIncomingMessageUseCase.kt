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

package studio.lunabee.onesafe.messaging.domain.usecase

import com.google.protobuf.InvalidProtocolBufferException
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRPrivateKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DoubleRatchetError
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.MessageHeader
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messaging.domain.MessagingConstant
import studio.lunabee.onesafe.messaging.domain.extension.asOSError
import studio.lunabee.onesafe.messaging.domain.extension.toInstant
import studio.lunabee.onesafe.messaging.domain.model.DecryptIncomingMessageData
import studio.lunabee.onesafe.messaging.domain.model.OSEncryptedMessage
import studio.lunabee.onesafe.messaging.domain.model.SharedMessage
import studio.lunabee.onesafe.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessagingCryptoRepository
import java.util.UUID
import javax.inject.Inject

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
                val error = (result as? LBResult.Failure)?.throwable as? OSError
                error?.code != OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY &&
                    error?.code != OSDomainError.Code.WRONG_CONTACT
            } ?: LBResult.Failure(OSDomainError(OSDomainError.Code.NO_MATCHING_CONTACT))
    }

    private suspend fun handleHandShakeMessage(
        handShakeMessage: OSMessage.HandShakeMessage,
        contact: Contact,
        localKey: ContactLocalKey,
    ): LBResult<DecryptIncomingMessageData> = when {
        // If the sharedConversationId don't correspond to the proto conversationId, it means that its the wrong contact
        handShakeMessage.conversationId != contact.sharedConversationId.toString() -> LBResult.Failure(
            OSDomainError(OSDomainError.Code.WRONG_CONTACT),
        )
        // If recipientID equals the contact id, it mean that its a message we send that message and we can't decrypt it
        handShakeMessage.recipientId == contact.id.toString() -> LBResult.Success(
            DecryptIncomingMessageData.DecryptOwnMessage(contact.id),
        )
        else -> OSError.runCatching {
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
            OSError.runCatching { decryptMessage(messageData, contact, encSharedKey, localKey) }
        } else {
            LBResult.Failure(OSDomainError(OSDomainError.Code.WRONG_CONTACT))
        }
    }

    private suspend fun decryptHandShake(
        plainMessageProto: OSMessage.HandShakeMessage,
        contact: Contact,
        localKey: ContactLocalKey,
    ): DecryptIncomingMessageData {
        val plainMessage = OSEncryptedMessage(
            body = plainMessageProto.body.toByteArray(),
            messageHeader = MessageHeader(
                messageNumber = plainMessageProto.header.messageNumber,
                sequenceNumber = plainMessageProto.header.sequenceMessageNumber,
                publicKey = DRPublicKey(plainMessageProto.header.publicKey.toByteArray()),
            ),
            recipientId = UUID.fromString(plainMessageProto.recipientId),
        )
        // We skip the handshake step if we already did one
        if (contact.encSharedKey == null) {
            val privateKey = getHandShakeDataUseCase(contact.id).data?.oneSafePrivateKey!!
            val sharedSecret = doubleRatchetKeyRepository.createDiffieHellmanSharedSecret(
                DRPublicKey(plainMessageProto.oneSafePublicKey.toByteArray()),
                DRPrivateKey(privateKey),
            )
            val encSharedKey = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(sharedSecret.value))
            contactRepository.addContactSharedKey(contact.id, ContactSharedKey(encSharedKey))

            // We remove handShake data since we don't need it anymore
            handShakeDataRepository.delete(contact.id)
        }
        val messageKey = try {
            doubleRatchetEngine.getReceiveKey(messageHeader = plainMessage.messageHeader, DoubleRatchetUUID(contact.id))
        } catch (e: DoubleRatchetError) {
            if (e.type == DoubleRatchetError.Type.MessageKeyNotFound) {
                return DecryptIncomingMessageData.AlreadyDecryptedMessage(contact.id)
            } else {
                throw e.asOSError()
            }
        }

        val plainBody = messagingCryptoRepository.decryptMessage(plainMessage.body, messageKey)
        val plainMessageDataProto = OSMessage.MessageData.parseFrom(plainBody)

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
        return DecryptIncomingMessageData.NewMessage(contact.id, osPlainMessage)
    }

    private suspend fun decryptMessage(
        messageData: ByteArray,
        contact: Contact,
        sharedKey: ContactSharedKey,
        localKey: ContactLocalKey,
    ): DecryptIncomingMessageData {
        val plainData = bubblesCryptoRepository.sharedDecrypt(messageData, localKey, sharedKey)
        val plainMessageProto = OSMessage.Message.parseFrom(plainData)
        val plainMessage = OSEncryptedMessage(
            body = plainMessageProto.body.toByteArray(),
            messageHeader = MessageHeader(
                messageNumber = plainMessageProto.header.messageNumber,
                sequenceNumber = plainMessageProto.header.sequenceMessageNumber,
                publicKey = DRPublicKey(plainMessageProto.header.publicKey.toByteArray()),
            ),
            recipientId = UUID.fromString(plainMessageProto.recipientId),
        )
        if (plainMessage.messageHeader.messageNumber == 0) {
            handShakeDataRepository.delete(contact.id)
        }

        return when (plainMessage.recipientId) {
            contact.id -> DecryptIncomingMessageData.DecryptOwnMessage(contact.id)
            else -> {
                val messageKey = try {
                    doubleRatchetEngine.getReceiveKey(messageHeader = plainMessage.messageHeader, DoubleRatchetUUID(contact.id))
                } catch (e: DoubleRatchetError) {
                    if (e.type == DoubleRatchetError.Type.MessageKeyNotFound) {
                        return DecryptIncomingMessageData.AlreadyDecryptedMessage(contact.id)
                    } else {
                        throw e.asOSError()
                    }
                }
                val plainBody = messagingCryptoRepository.decryptMessage(plainMessage.body, messageKey)
                val plainMessageDataProto = OSMessage.MessageData.parseFrom(plainBody)
                DecryptIncomingMessageData.NewMessage(
                    contact.id,
                    SharedMessage(
                        content = plainMessageDataProto.content,
                        recipientId = plainMessage.recipientId,
                        sentAt = plainMessageDataProto.sentAt.toInstant(),
                    ),
                )
            }
        }
    }

    private fun tryParseHandShakeMessage(messageData: ByteArray): OSMessage.HandShakeMessage? {
        return try {
            OSMessage.HandShakeMessage.parseFrom(messageData)
        } catch (e: InvalidProtocolBufferException) {
            null
        }
    }
}
