/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/3/2024 - for the oneSafe6 SDK.
 * Last modified 03/10/2024 09:30
 */

package studio.lunabee.onesafe.bubbles.ui.home.composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

@Composable
fun AddContactDropdownMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    onCreateContactClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        OSClickableRow(
            onClick = {
                onCreateContactClick()
                onDismiss()
            },
            text = LbcTextSpec.StringResource(OSString.bubbles_inviteContact),
            leadingIcon = {
                OSIconDecorationButton(image = OSImageSpec.Drawable(OSDrawable.ic_add))
            },
        )
        OSClickableRow(
            onClick = {
                onScanClick()
                onDismiss()
            },
            text = LbcTextSpec.StringResource(OSString.bubbles_scan),
            leadingIcon = {
                OSIconDecorationButton(image = OSImageSpec.Drawable(OSDrawable.ic_qr_scanner))
            },
        )
    }
}
