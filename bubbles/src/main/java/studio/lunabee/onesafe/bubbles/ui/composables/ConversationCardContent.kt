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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette

class ConversationCardContent(
    private val conversationInfo: BubblesConversationInfo,
    private val onClick: () -> Unit,
    override val key: Any = conversationInfo.id,
) : OSLazyCardContent.Item {
    override val contentType: Any = "ContactItem"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        val nameProvider = conversationInfo.nameProvider
        ConversationRow(
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
            hasUnreadMessage = conversationInfo.hasUnreadMessage,
        )
    }
}

@Composable
fun ConversationRow(
    paddingValues: PaddingValues,
    onClick: () -> Unit,
    osItemIllustration: OSItemIllustration,
    hasUnreadMessage: Boolean,
    label: LbcTextSpec,
    subtitle: LbcTextSpec?,
    itemSubtitleMaxLine: Int,
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(paddingValues),
    ) {
        if (hasUnreadMessage) {
            NotificationIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        horizontal = OSDimens.SystemSpacing.Regular,
                        vertical = OSDimens.SystemSpacing.Small,
                    ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            osItemIllustration.ImageComposable(contentDescription = null, style = OSSafeItemStyle.Small)
            Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
            Column {
                OSText(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSystemInDarkTheme()) LocalColorPalette.current.Neutral10 else Color.Unspecified,
                )
                if (subtitle != null) { // TODO removed isNotEmpty check, should be handle before
                    OSText(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSystemInDarkTheme()) LocalColorPalette.current.Neutral20 else LocalColorPalette.current.Neutral60,
                        maxLines = itemSubtitleMaxLine,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
