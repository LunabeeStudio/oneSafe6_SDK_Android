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
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRPrivateKey
import studio.lunabee.doubleratchet.model.DRPublicKey
import studio.lunabee.doubleratchet.model.DRSharedSecret
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
import studio.lunabee.messaging.domain.model.proto.ProtoResetInvitationMessage
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.BubblesCryptoError
import studio.lunabee.onesafe.error.BubblesDomainError
import studio.lunabee.onesafe.error.OSError
import kotlin.time.Clock
import kotlin.time.Instant

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
    private val updateContactResetConversationDateUseCase: UpdateContactResetConversationDateUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val clock: Clock,
) {
    /**
     * @param messageData The raw message
     *
     * @return failure with NO_MATCHING_CONTACT error code if no contact shared key has been found to decrypt the message.
     */
    suspend operator fun invoke(messageData: ByteArray): LBResult<DecryptIncomingMessageData> {
        // We try to parse the data as HandShakeMessage to know what kind of message we are dealing with
        val handShakeMessage = tryParseHandShakeMessage(messageData)
        return getAllContactsUseCase()
            .first()
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
            OSError.runCatching {
                decryptSharedEncryptedMessage(messageData, contact, encSharedKey, localKey)
            }
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
            recipientId = DoubleRatchetUUID.fromString(plainMessageProto.recipientId),
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
                date = plainMessageDataProto.sentAt.toInstant(),
                recipientId = plainMessage.recipientId,
            )
        }
        return DecryptIncomingMessageData.NewMessage(contact.id, osPlainMessage, messageKey)
    }

    /**
     * Decrypt the message with shared bubbles crypto.
     * - determine if the message is `ProtoMessage` or `ProtoResetInvitationMessage`
     * - Verify if the message is not outdated via the conversation reset date
     * - Handle message
     */
    private suspend fun decryptSharedEncryptedMessage(
        messageData: ByteArray,
        contact: Contact,
        sharedKey: ContactSharedKey,
        localKey: ContactLocalKey,
    ): DecryptIncomingMessageData {
        val plainData = bubblesCryptoRepository.sharedDecrypt(messageData, localKey, sharedKey)
        val plainMessageProto = tryParseMessage(plainData)
        val plainResetInvitationMessage = tryParseResetMessage(plainData)
        val contactResetConversationDate = contact.encResetConversationDate?.let {
            contactLocalDecryptUseCase.invoke(it, contact.id, Instant::class).data
        } ?: Instant.DISTANT_PAST
        val messageResetConversationDate = plainMessageProto?.conversationResetDate?.toInstant()
            ?: plainResetInvitationMessage?.conversationResetDate?.toInstant() ?: Instant.DISTANT_PAST
        return when {
            messageResetConversationDate < contactResetConversationDate ->
                DecryptIncomingMessageData
                    .OutdatedConversationMessage(
                        contact.id,
                    )
            plainMessageProto != null -> decryptSafeMessage(contact, plainMessageProto)
            plainResetInvitationMessage != null -> handleResetMessage(contact, plainResetInvitationMessage)
            else -> throw BubblesDomainError(BubblesDomainError.Code.NOT_A_BUBBLES_MESSAGE)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun decryptSafeMessage(
        contact: Contact,
        plainMessageProto: ProtoMessage,
    ): DecryptIncomingMessageData {
        val plainMessage = OSEncryptedMessage(
            body = plainMessageProto.body,
            messageHeader = MessageHeader(
                messageNumber = plainMessageProto.header.messageNumber,
                sequenceNumber = plainMessageProto.header.sequenceMessageNumber,
                publicKey = DRPublicKey(plainMessageProto.header.publicKey),
            ),
            recipientId = DoubleRatchetUUID.fromString(plainMessageProto.recipientId),
        )
        handShakeDataRepository.delete(contact.id)
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
                        date = plainMessageDataProto.sentAt.toInstant(),
                    ),
                    messageKey = messageKey,
                )
            }
        }
    }

    /**
     * Handle a reset invitation message,
     * - Replace old Double ratchet conversation by new one with new crypto
     * - Update the reset conversation date in the contact
     */
    private suspend fun handleResetMessage(
        contact: Contact,
        plainResetInvitationMessage: ProtoResetInvitationMessage,
    ): DecryptIncomingMessageData {
        val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
            contact.sharedConversationId,
            doubleRatchetKeyRepository.rootKeyByteSize,
        )
        doubleRatchetEngine.createNewConversationFromInvitation(
            newConversationId = contact.id,
            sharedSalt = DRSharedSecret(sharedSalt),
            contactPublicKey = DRPublicKey(plainResetInvitationMessage.doubleRatchetPublicKey),
        )
        updateContactResetConversationDateUseCase.invoke(
            contactId = contact.id,
            instant = plainResetInvitationMessage.conversationResetDate.toInstant(),
        )
        return DecryptIncomingMessageData.ResetMessage(
            contactId = contact.id,
            receivedAt = clock.now(),
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun tryParseHandShakeMessage(messageData: ByteArray): ProtoHandShakeMessage? {
        return try {
            val result = ProtoBuf.decodeFromByteArray<ProtoHandShakeMessage>(messageData)
            DoubleRatchetUUID.fromString(result.recipientId)
            DoubleRatchetUUID.fromString(result.conversationId)
            return result
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun tryParseResetMessage(messageData: ByteArray): ProtoResetInvitationMessage? {
        return try {
            val result = ProtoBuf.decodeFromByteArray<ProtoResetInvitationMessage>(messageData)
            DoubleRatchetUUID.fromString(result.recipientId)
            DoubleRatchetUUID.fromString(result.conversationId)
            return result
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun tryParseMessage(messageData: ByteArray): ProtoMessage? {
        return try {
            val result = ProtoBuf.decodeFromByteArray<ProtoMessage>(messageData)
            DoubleRatchetUUID.fromString(result.recipientId)
            return result
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }
}
