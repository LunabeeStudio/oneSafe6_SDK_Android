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
 * Last modified 6/14/23, 3:54 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.messaging.extension.isSameDayAs
import java.time.Instant
import java.util.UUID

sealed interface ConversationUiData {

    data class Message(
        val id: UUID,
        val text: LbcTextSpec,
        val direction: MessageDirection,
        val sendAt: Instant,
        val channelName: String?,
        val type: MessageType,
    ) : ConversationUiData {
        fun wereSentOnSameDay(other: Message?): Boolean =
            other?.let { sendAt.isSameDayAs(it.sendAt) } ?: false
    }

    data class DateHeader(
        val date: Instant,
    ) : ConversationUiData

    enum class MessageType {
        Message,
        Invitation,
    }
}
