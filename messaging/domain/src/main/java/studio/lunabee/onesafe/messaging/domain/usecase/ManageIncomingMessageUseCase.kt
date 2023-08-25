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

package studio.lunabee.onesafe.messaging.domain.usecase

import com.google.protobuf.InvalidProtocolBufferException
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.messagecompanion.OSMessage
import studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage
import javax.inject.Inject

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
            val result = decryptIncomingMessageUseCase(messageData = data)
            when (result) {
                is LBResult.Success -> {
                    val message = result.successData.second
                    val contact = result.successData.first
                    message?.let {
                        saveMessageUseCase(
                            plainMessage = message.content,
                            sentAt = message.sentAt,
                            contactId = contact.id,
                            recipientId = message.recipientId,
                            channel = channel,
                        )
                    }
                    LBResult.Success(ManagingIncomingMessageResultData.Message(result.successData))
                }
                is LBResult.Failure -> LBResult.Failure(result.throwable)
            }
        }
    }

    private fun tryParseInvitationMessage(messageData: ByteArray): Boolean {
        return try {
            OSMessage.InvitationMessage.parseFrom(messageData)
            true
        } catch (e: InvalidProtocolBufferException) {
            false
        }
    }
}

sealed interface ManagingIncomingMessageResultData {
    object Invitation : ManagingIncomingMessageResultData

    data class Message(
        val data: Pair<Contact, OSPlainMessage?>,
    ) : ManagingIncomingMessageResultData
}