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
 * Last modified 5/31/24, 5:15 PM
 */

package studio.lunabee.onesafe.bubbles.ui.conversation

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.first
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.messaging.domain.model.PlainMessageContentData
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.messaging.domain.usecase.DecryptSafeMessageUseCase
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import javax.inject.Inject

class ConversationSubtitleFromMessageDelegate @Inject constructor(
    private val decryptSafeMessageUseCase: DecryptSafeMessageUseCase,
    private val secureGetItemUseCase: SecureGetItemUseCase,
    private val itemDecryptUseCase: ItemDecryptUseCase,
) {
    suspend operator fun invoke(message: SafeMessage): ConversationSubtitle {
        val plainMessageContentData = decryptSafeMessageUseCase.content(message)
        val messageContent = when (plainMessageContentData) {
            PlainMessageContentData.AcceptedInvitation ->
                LbcTextSpec
                    .StringResource(OSString.bubbles_acceptedInvitation)
            is PlainMessageContentData.Default -> {
                when (val plainContent = plainMessageContentData.content) {
                    is LBResult.Failure ->
                        LbcTextSpec
                            .StringResource(OSString.bubbles_writeMessageScreen_corruptedMessage)
                    is LBResult.Success -> LbcTextSpec.Raw(plainContent.successData)
                }
            }
            is PlainMessageContentData.SafeItemSharing -> {
                val itemId = plainMessageContentData.itemId
                val item = itemId?.uuid?.let { secureGetItemUseCase.invoke(itemId.uuid).first() }
                val name = item?.encName?.let { itemDecryptUseCase(it, item.id, String::class).data }
                val label = name?.let(LbcTextSpec::Raw) ?: LbcTextSpec.StringResource(OSString.common_noName)
                when (message.direction) {
                    MessageDirection.SENT ->
                        LbcTextSpec
                            .StringResource(OSString.bubbles_message_subtitle_sharedItem, label)
                    MessageDirection.RECEIVED ->
                        LbcTextSpec
                            .StringResource(OSString.bubbles_message_subtitle_receivedItem, label)
                }
            }
            PlainMessageContentData.ResetConversation ->
                LbcTextSpec
                    .StringResource(OSString.bubbles_writeMessageScreen_conversationReset)
        }
        return ConversationSubtitle.Message(content = messageContent)
    }
}
