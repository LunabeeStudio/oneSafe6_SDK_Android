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
 * Created by Lunabee Studio / Date - 8/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/08/2024 15:32
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.model.MessageAction
import java.util.UUID

interface SafeItemMessageCombinedPress {

    @Composable
    fun Content(message: ConversationUiData.Message.SafeItem) {
    }

    fun onLongClick(messageId: UUID)

    fun onClick(itemId: UUID)
}

class DropDownSafeItemMessageCombinedPress(
    private val onNavigateToItemClick: (UUID) -> Unit,
    private val onDeleteMessageClick: (DoubleRatchetUUID) -> Unit,
) : SafeItemMessageCombinedPress {
    private val menuExpandedMessageIdFlow: MutableStateFlow<UUID?> = MutableStateFlow(null)

    @Composable
    override fun Content(message: ConversationUiData.Message.SafeItem) {
        val menuExpandedMessageId: UUID? by menuExpandedMessageIdFlow.collectAsStateWithLifecycle()
        val actions: List<MessageAction> = listOf(
            MessageAction.Delete { message.id.let(onDeleteMessageClick) },
        )
        MessageActionMenu(
            isMenuExpended = menuExpandedMessageId == message.id.uuid,
            onDismiss = { menuExpandedMessageIdFlow.value = null },
            actions = actions,
        )
    }

    override fun onLongClick(messageId: UUID) {
        menuExpandedMessageIdFlow.value = messageId
    }

    override fun onClick(itemId: UUID) {
        onNavigateToItemClick(itemId)
    }
}
