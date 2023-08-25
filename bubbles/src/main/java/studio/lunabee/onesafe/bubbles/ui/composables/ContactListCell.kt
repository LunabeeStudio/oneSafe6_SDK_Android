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
 * Created by Lunabee Studio / Date - 8/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/08/2023 10:06
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSColor

@Composable
fun ContactListCell(
    bubblesContactInfo: BubblesContactInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(paddingValues)
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bubblesContactInfo.nameProvider is EmojiNameProvider) {
            OSItemIllustration.Emoji(bubblesContactInfo.nameProvider.placeholderName, null)
        } else {
            OSItemIllustration.Text(bubblesContactInfo.nameProvider.placeholderName, null)
        }.ImageComposable(contentDescription = bubblesContactInfo.nameProvider.name, style = OSSafeItemStyle.Small)
        Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
        OSText(
            text = bubblesContactInfo.nameProvider.name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = NameMaxLine,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.0f),
            color = if (isSystemInDarkTheme()) OSColor.Neutral10 else Color.Unspecified,
        )
        if (bubblesContactInfo.conversationState == ConversationState.WaitingForReply) {
            OSRegularSpacer()
            PendingInputChip()
        }
    }
}

private const val NameMaxLine: Int = 2
