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
 * Created by Lunabee Studio / Date - 7/17/2023 - for the oneSafe6 SDK.
 * Last modified 17/07/2023 09:04
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.DecryptIncomingMessageData
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.model.proto.ProtoInvitationMessage
import studio.lunabee.onesafe.di.Inject

/**
 * Take data, and decide if its a textMessage, an invitation or noting and use the good use case according to what it is.
 */
class ManageIncomingMessageUseCase @Inject constructor(
    private val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
) {
    suspend operator fun invoke(data: ByteArray, channel: String?): LBResult<ManagingIncomingMessageResultData> {
        return if (tryParseInvitationMessage(data)) {
            LBResult.Success(ManagingIncomingMessageResultData.Invitation)
        } else {
            when (val result = decryptIncomingMessageUseCase(messageData = data)) {
                is LBResult.Success -> {
                    when (val successData = result.successData) {
                        is DecryptIncomingMessageData.ResetMessage -> LBResult.Success(manageResetMessage(successData))
                        is DecryptIncomingMessageData.NewMessage -> LBResult.Success(manageNewMessage(successData, channel))
                        is DecryptIncomingMessageData.AlreadyDecryptedMessage,
                        is DecryptIncomingMessageData.DecryptOwnMessage,
                        is DecryptIncomingMessageData.OutdatedConversationMessage,
                        -> {
                            LBResult.Success(
                                ManagingIncomingMessageResultData.Message(
                                    DecryptResult.fromDecryptIncomingMessageData(result.successData),
                                ),
                            )
                        }
                    }
                }
                is LBResult.Failure -> LBResult.Failure(result.throwable)
            }
        }
    }

    /**
     * Save a message with ResetConversationMessageData constant to indicate that the conversation has been reset in write message
     */
    private suspend fun manageResetMessage(result: DecryptIncomingMessageData.ResetMessage): ManagingIncomingMessageResultData {
        saveMessageUseCase(
            plainMessage = result.osPlainMessage,
            contactId = result.contactId,
            channel = null,
            id = createRandomUUID(),
            safeItemId = null,
        )
        return ManagingIncomingMessageResultData.Message(
            DecryptResult.fromDecryptIncomingMessageData(result),
        )
    }

    private suspend fun manageNewMessage(
        result: DecryptIncomingMessageData.NewMessage,
        channel: String?,
    ): ManagingIncomingMessageResultData {
        val message: SharedMessage? = result.osPlainMessage
        return when (message?.content) {
            MessagingConstant.SafeItemMessageData -> {
                ManagingIncomingMessageResultData.SafeItem(
                    decryptResult = DecryptResult.fromDecryptIncomingMessageData(result),
                    sharedMessage = message,
                )
            }
            else -> {
                val contactId = result.contactId
                message?.let {
                    saveMessageUseCase(
                        plainMessage = message,
                        contactId = contactId,
                        channel = channel,
                        id = createRandomUUID(),
                        safeItemId = null,
                    )
                }
                ManagingIncomingMessageResultData.Message(DecryptResult.fromDecryptIncomingMessageData(result))
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun tryParseInvitationMessage(messageData: ByteArray): Boolean {
        return try {
            val result = ProtoBuf.decodeFromByteArray<ProtoInvitationMessage>(messageData)
            DoubleRatchetUUID.fromString(result.recipientId)
            DoubleRatchetUUID.fromString(result.conversationId)
            true
        } catch (e: SerializationException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: NullPointerException) {
            false
        } catch (e: IndexOutOfBoundsException) {
            false
        }
    }
}

sealed interface ManagingIncomingMessageResultData {
    data object Invitation : ManagingIncomingMessageResultData

    data class Message(
        val decryptResult: DecryptResult,
    ) : ManagingIncomingMessageResultData

    data class SafeItem(
        val decryptResult: DecryptResult,
        val sharedMessage: SharedMessage,
    ) : ManagingIncomingMessageResultData
}
