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
 * Last modified 17/08/2023 10:01
 */

package studio.lunabee.onesafe.messaging.writemessage.composable.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.titleMediumBlack

@Composable
fun AppWriteMessageTopBar(
    contactNameProvider: OSNameProvider,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSeeContactClick: () -> Unit,
    onDeleteAllMessagesClick: () -> Unit,
) {
    val illustration: OSItemIllustration by remember(contactNameProvider) {
        mutableStateOf(OSItemIllustrationHelper.get(contactNameProvider))
    }
    var isActionMenuExpanded: Boolean by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Medium),
    ) {
        OSIconButton(
            image = OSImageSpec.Drawable(R.drawable.ic_back),
            onClick = onBack,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
            colors = OSIconButtonDefaults.iconButtonColors(
                containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                state = OSActionState.Enabled,
            ),
        )
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                modifier = Modifier
                    .testTag(UiConstants.TestTag.Item.OneSafeKWriteMessageRecipientCard)
                    .clip(RoundedCornerShape(OSDimens.SystemCornerRadius.Regular))
                    .background(LocalDesignSystem.current.bubblesSecondaryContainer())
                    .padding(
                        horizontal = OSDimens.SystemSpacing.Medium,
                        vertical = OSDimens.SystemSpacing.Small,
                    )
                    .weight(1f),
            ) {
                illustration.ImageComposable(contentDescription = null, style = OSSafeItemStyle.Tiny)
                OSText(
                    text = contactNameProvider.name,
                    style = MaterialTheme.typography.titleMediumBlack,
                    modifier = Modifier,
                )
            }
            OSSmallSpacer()
            OSIconButton(
                image = OSImageSpec.Drawable(R.drawable.ic_more),
                onClick = { isActionMenuExpanded = true },
                colors = OSIconButtonDefaults.secondaryIconButtonColors(state = OSActionState.Enabled),
            )
            ContactActionMenu(
                isMenuExpended = isActionMenuExpanded,
                onDismiss = { isActionMenuExpanded = false },
                onSeeContactClick = {
                    isActionMenuExpanded = false
                    onSeeContactClick()
                },
                onDeleteMessages = {
                    isActionMenuExpanded = false
                    onDeleteAllMessagesClick()
                },
            )
        }
    }
}

@Preview
@Composable
fun AppWriteMessageTopBarPreview() {
    OSPreviewBackgroundTheme {
        AppWriteMessageTopBar(
            contactNameProvider = DefaultNameProvider(loremIpsum(5)),
            onBack = {},
            onSeeContactClick = {},
            onDeleteAllMessagesClick = {},
        )
    }
}