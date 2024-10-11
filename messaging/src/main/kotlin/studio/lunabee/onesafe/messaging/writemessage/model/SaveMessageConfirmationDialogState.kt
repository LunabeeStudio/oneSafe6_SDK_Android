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
 * Last modified 03/10/2024 13:34
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class SaveMessageConfirmationDialogState(
    onConfirm: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_unknownSharingStatus_alertTitle)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_unknownSharingStatus_message)
    override val customContent: @Composable (() -> Unit)? = null
    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_unknownSharingStatus_notSent),
            type = DialogAction.Type.Dangerous,
            onClick = dismiss,
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_unknownSharingStatus_sent),
            type = DialogAction.Type.Normal,
            onClick = {
                onConfirm()
                dismiss()
            },
        ),
    )
}
