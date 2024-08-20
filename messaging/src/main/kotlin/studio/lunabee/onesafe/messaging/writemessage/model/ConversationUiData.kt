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
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.messaging.extension.isSameDayAs
import studio.lunabee.onesafe.model.OSItemIllustration
import java.time.Instant
import java.util.UUID

sealed interface ConversationUiData {

    sealed interface Message : ConversationUiData {

        val sendAt: Instant?
        val id: DoubleRatchetUUID
        val direction: MessageDirection
        val channelName: String?

        data class Text(
            override val id: DoubleRatchetUUID,
            override val direction: MessageDirection,
            override val sendAt: Instant?,
            override val channelName: String?,
            val hasCorruptedData: Boolean,
            val text: LbcTextSpec,
            val type: MessageType,
        ) : Message

        data class SafeItem(
            override val id: DoubleRatchetUUID,
            override val direction: MessageDirection,
            override val sendAt: Instant?,
            override val channelName: String?,
            val itemId: UUID?,
            val name: OSNameProvider,
            val identifier: LbcTextSpec?,
            val icon: OSItemIllustration,
        ) : Message

        fun wereSentOnSameDay(other: Message?): Boolean =
            other?.sendAt?.let { sendAt?.isSameDayAs(it) } ?: false
    }

    data class DateHeader(
        val date: Instant,
    ) : ConversationUiData

    enum class MessageType {
        Message,
        Invitation,
    }
}
