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
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.MessageHeader
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.messaging.domain.model.HandShakeData
import studio.lunabee.messaging.domain.model.proto.ProtoHandShakeMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessage
import studio.lunabee.messaging.domain.model.proto.ProtoMessageData
import studio.lunabee.messaging.domain.model.proto.ProtoMessageHeader
import studio.lunabee.messaging.domain.model.proto.ProtoTimestamp
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.onesafe.error.OSError

/**
 * Encrypt a message for a contact and encode it on base64
 */
class EncryptMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val messagingCryptoRepository: MessagingCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val contactRepository: ContactRepository,
    private val getHandShakeDataUseCase: GetHandShakeDataUseCase,
    private val localDecryptUseCase: ContactLocalDecryptUseCase,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend operator fun invoke(
        plainMessage: String,
        contactId: DoubleRatchetUUID,
        sentAt: Instant,
        sendMessageData: SendMessageData,
    ): LBResult<ByteArray> = OSError.runCatching {
        val messageBody = ProtoMessageData(
            content = plainMessage,
            sentAt = ProtoTimestamp.fromInstant(sentAt),
        )
        val byteArrayMessage = ProtoBuf.encodeToByteArray(messageBody)
        // Encrypt the message body with the message Key
        val encryptedMessageBody: ByteArray = messagingCryptoRepository.encryptMessage(byteArrayMessage, sendMessageData.messageKey)
        val handShakeDataRes: LBResult<HandShakeData?> = getHandShakeDataUseCase(contactId)
        when (handShakeDataRes) {
            is LBResult.Failure -> return LBResult.Failure(handShakeDataRes.throwable)
            is LBResult.Success -> {
                val handShakeData = handShakeDataRes.successData
                if (handShakeData != null) {
                    createHandShakeMessage(
                        encryptedMessageBody = encryptedMessageBody,
                        messageHeader = sendMessageData.messageHeader,
                        contactId = contactId,
                        handShakeData = handShakeData,
                    )
                } else {
                    createEncryptedMessage(
                        encryptedMessageBody = encryptedMessageBody,
                        messageHeader = sendMessageData.messageHeader,
                        contactId = contactId,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun createEncryptedMessage(
        encryptedMessageBody: ByteArray,
        messageHeader: MessageHeader,
        contactId: DoubleRatchetUUID,
    ): ByteArray {
        val sharedKey = contactRepository.getSharedKey(contactId)
            ?: throw BubblesMessagingError(BubblesMessagingError.Code.CONTACT_NOT_FOUND)
        val localKey = contactKeyRepository.getContactLocalKey(contactId)
        val contact = contactRepository.getContact(contactId)
        val conversationResetDate = contact?.encResetConversationDate?.let {
            localDecryptUseCase(it, contactId, Instant::class).data
        } ?: Instant.DISTANT_PAST
        val message = ProtoMessage(
            body = encryptedMessageBody,
            header = ProtoMessageHeader(
                messageNumber = messageHeader.messageNumber,
                sequenceMessageNumber = messageHeader.sequenceNumber,
                publicKey = messageHeader.publicKey.value,
            ),
            recipientId = contactId.uuidString(),
            conversationResetDate = ProtoTimestamp.fromInstant(conversationResetDate),
        )
        val messageByteArray = ProtoBuf.encodeToByteArray(message)
        return bubblesCryptoRepository.sharedEncrypt(messageByteArray, localKey, sharedKey)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createHandShakeMessage(
        encryptedMessageBody: ByteArray,
        messageHeader: MessageHeader,
        contactId: DoubleRatchetUUID,
        handShakeData: HandShakeData,
    ): ByteArray {
        val message = ProtoHandShakeMessage(
            body = encryptedMessageBody,
            header = ProtoMessageHeader(
                messageNumber = messageHeader.messageNumber,
                sequenceMessageNumber = messageHeader.sequenceNumber,
                publicKey = messageHeader.publicKey.value,
            ),
            conversationId = handShakeData.conversationSharedId.uuidString(),
            oneSafePublicKey = handShakeData.oneSafePublicKey!!,
            recipientId = contactId.uuidString(),
        )
        return ProtoBuf.encodeToByteArray(message)
    }
}
