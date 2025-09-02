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
 * Last modified 5/31/24, 2:37 PM
 */

package studio.lunabee.messaging.domain.usecase

import kotlin.time.Instant
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.PlainMessageContentData
import studio.lunabee.messaging.domain.model.PlainMessageData
import studio.lunabee.messaging.domain.model.SafeMessage

/**
 * Decrypt a [SafeMessage] into a [PlainMessageData] or only the content to a [PlainMessageContentData]
 * Takes care of checking if the message is an invitation response or a real message
 */
class DecryptSafeMessageUseCase @Inject constructor(
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
) {
    /**
     * Decrypt all message data to [PlainMessageData]
     */
    suspend fun message(message: SafeMessage): PlainMessageData {
        val sentAt = contactLocalDecryptUseCase(message.encSentAt, message.fromContactId, Instant::class)
        val plainMessageContentData = content(message)
        val channel = message.encChannel?.let { encChannel ->
            contactLocalDecryptUseCase(encChannel, message.fromContactId, String::class)
        }
        return when (plainMessageContentData) {
            PlainMessageContentData.AcceptedInvitation -> PlainMessageData.AcceptedInvitation(
                id = message.id,
                sentAt = sentAt,
                channel = channel,
                isRead = message.isRead,
            )
            is PlainMessageContentData.Default -> PlainMessageData.Default(
                id = message.id,
                sentAt = sentAt,
                content = plainMessageContentData.content,
                direction = message.direction,
                channel = channel,
                isRead = message.isRead,
            )
            is PlainMessageContentData.SafeItemSharing -> {
                PlainMessageData.SafeItem(
                    id = message.id,
                    sentAt = sentAt,
                    direction = message.direction,
                    channel = channel,
                    isRead = message.isRead,
                    itemId = plainMessageContentData.itemId,
                )
            }
            is PlainMessageContentData.ResetConversation -> {
                PlainMessageData.ResetConversation(
                    id = message.id,
                    sentAt = sentAt,
                    channel = channel,
                    isRead = message.isRead,
                )
            }
        }
    }

    /**
     * Decrypt the message content to a [PlainMessageContentData]
     */
    suspend fun content(message: SafeMessage): PlainMessageContentData {
        val content = contactLocalDecryptUseCase(message.encContent, message.fromContactId, String::class)
        return when (content.data) {
            MessagingConstant.FirstMessageData -> PlainMessageContentData.AcceptedInvitation
            MessagingConstant.ResetConversationMessageData -> PlainMessageContentData.ResetConversation
            MessagingConstant.SafeItemMessageData -> PlainMessageContentData.SafeItemSharing(
                message.encSafeItemId?.let { encSafeItemId ->
                    contactLocalDecryptUseCase(
                        encSafeItemId,
                        message.fromContactId,
                        DoubleRatchetUUID::class,
                    ).data
                },
            )
            else -> PlainMessageContentData.Default(content = content)
        }
    }
}
