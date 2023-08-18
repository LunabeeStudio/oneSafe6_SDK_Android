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
 * Created by Lunabee Studio / Date - 8/17/2023 - for the oneSafe6 SDK.
 * Last modified 17/08/2023 15:07
 */

package studio.lunabee.onesafe.bubbles.ui.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSNameProvider
import java.util.UUID

data class BubblesConversationInfo(
    val id: UUID,
    val nameProvider: OSNameProvider,
    val subtitle: ConversationSubtitle?,
)

sealed interface ConversationSubtitle {
    data class Message(
        val content: LbcTextSpec,
    ) : ConversationSubtitle

    data object NotReady : ConversationSubtitle
}
