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
 * Last modified 17/08/2023 10:33
 */

package studio.lunabee.onesafe.messaging.writemessage.composable.topbar

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState

@Composable
fun ContactActionMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    onDeleteMessages: () -> Unit,
    onHideConversation: () -> Unit,
    isConversationHidden: Boolean,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = modifier,
    ) {
        OSClickableRow(
            onClick = onHideConversation,
            text =
                if (isConversationHidden) {
                    LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_showConversation)
                } else {
                    LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_hideConversation)
                },
            leadingIcon = {
                OSIconDecorationButton(
                    image = if (isConversationHidden) {
                        OSImageSpec.Drawable(OSDrawable.ic_visibility_on)
                    } else {
                        OSImageSpec.Drawable(OSDrawable.ic_visibility_off)
                    },
                )
            },
            buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
        )
        OSClickableRow(
            text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_action_deleteAllMessages),
            onClick = onDeleteMessages,
            leadingIcon = { OSIconAlertDecorationButton(OSImageSpec.Drawable(drawable = OSDrawable.ic_delete)) },
            buttonColors = OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled),
        )
    }
}
