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
 * Created by Lunabee Studio / Date - 8/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/08/2024 15:32
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.commonui.OSNameProvider

data class WriteContactInfo(
    val id: DoubleRatchetUUID,
    val nameProvider: OSNameProvider,
    val messageSharingMode: MessageSharingMode,
    val conversationState: WriteConversationState,
    val isCorrupted: Boolean,
)

enum class WriteConversationState {
    WaitingForReply,
    Reset,
    Ready,
}
