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
 * Last modified 17/08/2023 15:22
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSItemRow

class ConversationCardContent(
    private val conversationInfo: BubblesConversationInfo,
    private val onClick: () -> Unit,
    override val key: Any = conversationInfo.id,
) : OSLazyCardContent.Item {
    override val contentType: Any = "ContactItem"

    @Composable
    override fun Content(padding: PaddingValues) {
        val nameProvider = conversationInfo.nameProvider
        OSItemRow(
            osItemIllustration = if (nameProvider is EmojiNameProvider) {
                OSItemIllustration.Emoji(nameProvider.placeholderName, null)
            } else {
                OSItemIllustration.Text(nameProvider.placeholderName, null)
            },
            label = nameProvider.name,
            paddingValues = padding,
            onClick = onClick,
            subtitle = when (conversationInfo.subtitle) {
                is ConversationSubtitle.Message -> conversationInfo.subtitle.content
                ConversationSubtitle.NotReady -> LbcTextSpec.StringResource(
                    R.string.bubbles_conversationScreen_waitingForResponse,
                    conversationInfo.nameProvider.name,
                )
                null -> null
            },
            itemSubtitleMaxLine = when (conversationInfo.subtitle) {
                is ConversationSubtitle.Message -> 1
                ConversationSubtitle.NotReady -> 2
                null -> 1
            },
        )
    }
}
