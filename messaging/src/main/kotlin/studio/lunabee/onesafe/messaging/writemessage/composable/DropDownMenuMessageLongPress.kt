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
 * Created by Lunabee Studio / Date - 9/5/2023 - for the oneSafe6 SDK.
 * Last modified 9/5/23, 6:28 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.model.MessageAction
import java.util.UUID

class DropDownMenuMessageLongPress(
    private val onResendClick: (UUID) -> Unit,
    private val onDeleteMessageClick: (UUID) -> Unit,
) : MessageLongPress() {

    private val menuExpandedMessageIdFlow: MutableStateFlow<UUID?> = MutableStateFlow(null)

    @Composable
    override fun Content(
        message: ConversationUiData.Message,
    ) {
        when (message.type) {
            ConversationUiData.MessageType.Message -> {
                val menuExpandedMessageId: UUID? by menuExpandedMessageIdFlow.collectAsStateWithLifecycle()
                val messageText = message.text.string
                val context = LocalContext.current

                val actions: List<MessageAction> = if (message.hasCorruptedData) {
                    listOf(MessageAction.Delete { message.id.uuid.let(onDeleteMessageClick) })
                } else {
                    when (message.direction) {
                        MessageDirection.SENT -> listOf(
                            MessageAction.Resend { message.id.uuid.let(onResendClick) },
                            MessageAction.Copy {
                                context.copyToClipBoard(
                                    messageText,
                                    LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_copyLabel),
                                )
                            },
                            MessageAction.Delete { message.id.uuid.let(onDeleteMessageClick) },
                        )
                        MessageDirection.RECEIVED -> listOf(
                            MessageAction.Copy {
                                context.copyToClipBoard(
                                    messageText,
                                    LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_copyLabel),
                                )
                            },
                            MessageAction.Delete { message.id.uuid.let(onDeleteMessageClick) },
                        )
                    }
                }

                MessageActionMenu(
                    isMenuExpended = menuExpandedMessageId == message.id.uuid,
                    onDismiss = { menuExpandedMessageIdFlow.value = null },
                    actions = actions,
                )
            }
            ConversationUiData.MessageType.Invitation -> {
                /* no-op */
            }
        }
    }

    override fun onLongClick(id: UUID) {
        menuExpandedMessageIdFlow.value = id
    }
}
