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
 * Created by Lunabee Studio / Date - 8/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/08/2023 16:00
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.messaging.writemessage.model.MessageAction
import studio.lunabee.onesafe.model.OSActionState

@Composable
fun MessageActionMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<MessageAction>,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        actions.forEach { action ->
            OSClickableRow(
                onClick = {
                    action.onClick()
                    onDismiss()
                },
                text = action.text,
                leadingIcon = {
                    if (action.isCritical) {
                        OSIconAlertDecorationButton(image = OSImageSpec.Drawable(action.icon))
                    } else {
                        OSIconDecorationButton(image = OSImageSpec.Drawable(action.icon))
                    }
                },
                buttonColors = if (action.isCritical) {
                    OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled)
                } else {
                    OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled)
                },
            )
        }
    }
}
