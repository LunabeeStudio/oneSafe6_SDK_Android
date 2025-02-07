package studio.lunabee.onesafe.feature.itemform.screen

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class DeleteFieldConfirmationDialogState(
    confirm: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {

    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)

    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.itemForm_deleteField_dialog_message)

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.itemForm_deleteField_dialog_confirmButton),
            type = DialogAction.Type.Dangerous,
            onClick = {
                confirm()
                dismiss()
            },
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
