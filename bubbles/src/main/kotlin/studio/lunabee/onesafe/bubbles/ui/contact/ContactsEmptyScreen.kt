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
 * Created by Lunabee Studio / Date - 8/25/2023 - for the oneSafe6 SDK.
 * Last modified 8/25/23, 5:37 PM
 */

package studio.lunabee.onesafe.bubbles.ui.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun EmptyContactsScreen(
    onAddContactClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(OSDimens.SystemSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
    ) {
        OSMessageCard(
            description = LbcTextSpec.StringResource(OSString.bubbles_inviteContact_description),
            action = {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.bubbles_inviteContact),
                    onClick = onAddContactClick,
                    buttonColors = OSFilledButtonDefaults.primaryButtonColors(),
                    leadingIcon = {
                        OSImage(image = OSImageSpec.Drawable(OSDrawable.ic_add))
                    },
                    modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                )
            },
        )
        OSMessageCard(
            description = LbcTextSpec.StringResource(OSString.bubbles_scan_description),
            action = {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.bubbles_scan),
                    onClick = onScanClick,
                    buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                    leadingIcon = {
                        OSImage(image = OSImageSpec.Drawable(OSDrawable.ic_qr_scanner))
                    },
                    modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                )
            },
        )
        // TODO = Implement the know more screen
    }
}

@OsDefaultPreview
@Composable
private fun EmptyContactsScreenPreview() {
    OSPreviewBackgroundTheme {
        EmptyContactsScreen(
            onAddContactClick = {},
            onScanClick = {},
            modifier = Modifier.Companion.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab),
        )
    }
}
