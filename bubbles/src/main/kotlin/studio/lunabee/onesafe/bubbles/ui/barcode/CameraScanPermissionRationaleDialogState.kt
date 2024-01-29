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
 * Created by Lunabee Studio / Date - 8/24/2023 - for the oneSafe6 SDK.
 * Last modified 8/24/23, 10:42 AM
 */

package studio.lunabee.onesafe.bubbles.ui.barcode

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

internal class CameraScanPermissionRationaleDialogState(
    launchPermissionRequest: () -> Unit,
    override val dismiss: () -> Unit,
    private val retry: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(LbcTextSpec.StringResource(OSString.common_cancel)) {
            dismiss()
        },
        DialogAction(LbcTextSpec.StringResource(OSString.common_ok)) {
            launchPermissionRequest()
            retry()
        },
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_scanbarcodeScreen_permission_rationale_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_scanbarcodeScreen_permission_rationale_message)
    override val customContent: (@Composable () -> Unit)? = null
}
