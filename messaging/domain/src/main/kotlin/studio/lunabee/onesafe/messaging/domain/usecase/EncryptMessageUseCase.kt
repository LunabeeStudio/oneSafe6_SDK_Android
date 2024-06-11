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

import com.google.protobuf.kotlin.toByteString
import com.google.protobuf.timestamp
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.doubleratchet.model.MessageHeader
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messagecompanion.handShakeMessage
import studio.lunabee.onesafe.messagecompanion.message
import studio.lunabee.onesafe.messagecompanion.messageData
import studio.lunabee.onesafe.messagecompanion.messageHeader
import studio.lunabee.onesafe.messaging.domain.extension.withInstant
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import studio.lunabee.onesafe.messaging.domain.repository.MessagingCryptoRepository
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Encrypt a message for a contact and encode it on base64
 */
class EncryptMessageUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val messagingCryptoRepository: MessagingCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val contactRepository: ContactRepository,
    private val getHandShakeDataUseCase: GetHandShakeDataUseCase,
) {
    suspend operator fun invoke(
        plainMessage: String,
        contactId: UUID,
        sentAt: Instant,
        sendMessageData: SendMessageData,
    ): LBResult<String> = OSError.runCatching {
        val messageBody: OSMessage.MessageData = messageData {
            this.content = plainMessage
            this.sentAt = timestamp { withInstant(sentAt) }
        }
        // Encrypt the message body with the message Key
        val encryptedMessageBody: ByteArray = ByteArrayOutputStream().use { byteArrayOutputStream ->
            messagingCryptoRepository.encryptMessage(byteArrayOutputStream, sendMessageData.messageKey).use { outputStream ->
                messageBody.writeTo(outputStream)
            }
            byteArrayOutputStream.toByteArray()
        }
        val handShakeDataRes = getHandShakeDataUseCase(contactId)
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

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun createEncryptedMessage(
        encryptedMessageBody: ByteArray,
        messageHeader: MessageHeader,
        contactId: UUID,
    ): String {
        val sharedKey = contactRepository.getSharedKey(contactId)
            ?: throw OSStorageError(OSStorageError.Code.CONTACT_NOT_FOUND)
        val localKey = contactKeyRepository.getContactLocalKey(contactId)
        val message: OSMessage.Message = message {
            body = encryptedMessageBody.toByteString()
            header = messageHeader {
                messageNumber = messageHeader.messageNumber
                sequenceMessageNumber = messageHeader.sequenceNumber
                publicKey = messageHeader.publicKey.value.toByteString()
            }
            recipientId = contactId.toString()
        }
        return ByteArrayOutputStream().use { byteArrayOutputStream ->
            // Decrypt the sharedKey and encrypt the whole message with it
            bubblesCryptoRepository.sharedEncrypt(byteArrayOutputStream, localKey, sharedKey).use { outputStream ->
                message.writeTo(outputStream)
            }
            Base64.encode(byteArrayOutputStream.toByteArray())
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun createHandShakeMessage(
        encryptedMessageBody: ByteArray,
        messageHeader: MessageHeader,
        contactId: UUID,
        handShakeData: HandShakeData,
    ): String {
        val message: OSMessage.HandShakeMessage = handShakeMessage {
            body = encryptedMessageBody.toByteString()
            header = messageHeader {
                messageNumber = messageHeader.messageNumber
                sequenceMessageNumber = messageHeader.sequenceNumber
                publicKey = messageHeader.publicKey.value.toByteString()
            }
            conversationId = handShakeData.conversationSharedId.toString()
            oneSafePublicKey = handShakeData.oneSafePublicKey!!.toByteString()
            recipientId = contactId.toString()
        }
        return Base64.encode(message.toByteArray())
    }
}
