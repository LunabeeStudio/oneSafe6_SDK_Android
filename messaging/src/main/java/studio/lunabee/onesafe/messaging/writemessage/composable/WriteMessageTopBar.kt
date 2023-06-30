/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 6/14/23, 2:49 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.R
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.titleMediumBlack

@Composable
fun WriteMessageTopBar(
    contactNameProvider: OSNameProvider,
    exitIcon: WriteMessageExitIcon,
    modifier: Modifier = Modifier,
    onClickOnChange: () -> Unit,
) {
    val illustration: OSItemIllustration by remember(contactNameProvider) {
        mutableStateOf(OSItemIllustrationHelper.get(contactNameProvider))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Medium),
    ) {
        exitIcon.Content()

        Box(
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.OneSafeKWriteMessageRecipientCard)
                .clip(RoundedCornerShape(OSDimens.SystemCornerRadius.Regular))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { onClickOnChange() },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                modifier = Modifier
                    .padding(
                        horizontal = OSDimens.SystemSpacing.Medium,
                        vertical = OSDimens.SystemSpacing.Small,
                    ),
            ) {
                illustration.ImageComposable(contentDescription = null, style = OSSafeItemStyle.Tiny)
                OSText(
                    text = contactNameProvider.name,
                    style = MaterialTheme.typography.titleMediumBlack,
                    modifier = Modifier.weight(1f),
                )
                OSText(
                    text = LbcTextSpec.StringResource(R.string.oneSafeK_changeRecipientButton_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Preview
@Composable
fun WriteMessageTopBarPreview() {
    OSPreviewBackgroundTheme {
        WriteMessageTopBar(
            contactNameProvider = DefaultNameProvider(loremIpsum(5)),
            exitIcon = WriteMessageExitIcon.WriteMessageCloseIcon {},
            onClickOnChange = {},
        )
    }
}
