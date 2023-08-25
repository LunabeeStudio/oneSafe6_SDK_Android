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
 * Created by Lunabee Studio / Date - 7/17/2023 - for the oneSafe6 SDK.
 * Last modified 7/17/23, 2:07 PM
 */

package studio.lunabee.onesafe.bubbles.ui.contact

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class ConfirmDeleteContactDialogState(
    override val dismiss: () -> Unit,
    deleteAction: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_delete_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_delete_description)
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_delete_confirm),
            type = DialogAction.Type.Dangerous,
            onClick = deleteAction,
        ),
    )
}