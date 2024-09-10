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
 * Created by Lunabee Studio / Date - 5/31/2024 - for the oneSafe6 SDK.
 * Last modified 5/31/24, 3:32 PM
 */

package studio.lunabee.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.datetime.Clock
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.messaging.domain.model.SharedMessage

/**
 * Generate the response to an invitation and store it if needed (only store the response once)
 */
class GetInvitationResponseMessageUseCase @Inject constructor(
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(contactId: DoubleRatchetUUID): LBResult<ByteArray> {
        val messageData: LBResult<SendMessageData> = getSendMessageDataUseCase(contactId)
        return when (messageData) {
            is LBResult.Failure -> LBResult.Failure(messageData.throwable)
            is LBResult.Success -> getAndSaveMessageIfNeeded(contactId, messageData)
        }
    }

    private suspend fun getAndSaveMessageIfNeeded(
        contactId: DoubleRatchetUUID,
        messageData: LBResult.Success<SendMessageData>,
    ): LBResult<ByteArray> {
        return encryptMessageUseCase(
            plainMessage = MessagingConstant.FirstMessageData,
            contactId = contactId,
            sentAt = clock.now(),
            sendMessageData = messageData.successData,
        ).also { message ->
            if (message is LBResult.Success) {
                val isFirstMessage = messageData.successData.messageHeader.messageNumber == 0
                if (isFirstMessage) {
                    saveMessageInDatabase(message.successData, contactId)
                }
            }
        }
    }

    private suspend fun saveMessageInDatabase(
        messageData: ByteArray,
        contactId: DoubleRatchetUUID,
    ) {
        saveSentMessageUseCase(
            plainMessage = SharedMessage(
                content = MessagingConstant.FirstMessageData,
                recipientId = contactId,
                sentAt = clock.now(),
            ),
            messageString = messageData,
            contactId = contactId,
            createdAt = clock.now(),
            channel = null,
        )
    }
}
