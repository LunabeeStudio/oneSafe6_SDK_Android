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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 3:00 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.factory

import android.content.Context
import androidx.compose.foundation.lazy.LazyListScope
import androidx.paging.compose.LazyPagingItems
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.messaging.utils.MessageSectionDateFormatter
import studio.lunabee.onesafe.messaging.writemessage.composable.ConversationDayHeader
import studio.lunabee.onesafe.messaging.writemessage.composable.MessageRow
import studio.lunabee.onesafe.messaging.writemessage.composable.MessageTextLongPress
import studio.lunabee.onesafe.messaging.writemessage.composable.SafeItemMessageCombinedPress
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData

object WriteMessageFactory {
    @Suppress("LongParameterList")
    fun addPagingConversation(
        lazyListScope: LazyListScope,
        conversation: LazyPagingItems<ConversationUiData>,
        contactNameProvider: OSNameProvider,
        messageTextLongPress: MessageTextLongPress,
        safeItemMessageCombinedPress: SafeItemMessageCombinedPress,
        context: Context,
    ) {
        val messageSectionDateFormatter = MessageSectionDateFormatter(context)
        lazyListScope.items(
            count = conversation.itemCount,
            contentType = { index ->
                when (conversation.peek(index)) {
                    is ConversationUiData.Message -> MessageContentType
                    is ConversationUiData.DateHeader -> DateHeaderContentType
                    else -> null
                }
            },
            key = { index ->
                when (val item = conversation.peek(index)) {
                    is ConversationUiData.Message -> item.id.uuid
                    else -> index
                }
            },
        ) { index ->
            conversation[index]?.let { item ->
                when (item) {
                    is ConversationUiData.Message -> {
                        MessageRow(
                            messageData = item,
                            contactName = contactNameProvider,
                            messageTextLongPress = messageTextLongPress,
                            safeItemMessageCombinedPress = safeItemMessageCombinedPress,
                        )
                    }
                    is ConversationUiData.DateHeader -> ConversationDayHeader(
                        text = messageSectionDateFormatter(item.date),
                    )
                }
            }
        }
    }

    private const val DateHeaderContentType: String = "DateHeaderContentType"
    private const val MessageContentType: String = "MessageContentType"
}
