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
 * Created by Lunabee Studio / Date - 7/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/07/2023 10:53
 */

package studio.lunabee.onesafe.bubbles.ui.contact

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

@Composable
fun FilledContactsScreen(
    onAddContactClick: () -> Unit,
    onScanClick: () -> Unit,
    contacts: List<UIBubblesContactInfo>,
    onContactClick: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
    ) {
        item {
            OSCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OSClickableRow(
                    text = LbcTextSpec.StringResource(OSString.bubbles_inviteContact),
                    onClick = onAddContactClick,
                    buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_add)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 0,
                        elementsCount = 2,
                    ),
                )
                OSClickableRow(
                    text = LbcTextSpec.StringResource(OSString.bubbles_scanQRCode),
                    onClick = onScanClick,
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_qr_scanner)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 1,
                        elementsCount = 2,
                    ),
                )
            }
        }
        lazyVerticalOSRegularSpacer()
        ContactScreenFactory.addContacts(
            contacts = contacts,
            onClick = onContactClick,
        )
    }
}

@OsDefaultPreview
@Composable
private fun FilledContactsScreenPreview() {
    OSPreviewBackgroundTheme {
        FilledContactsScreen(
            onAddContactClick = {},
            onScanClick = {},
            contacts = ConversationState.entries.map { state ->
                UIBubblesContactInfo(UUID.randomUUID(), OSNameProvider.fromName(state.name, false), state)
            },
            onContactClick = {},
            modifier = Modifier.Companion.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab),
        )
    }
}
