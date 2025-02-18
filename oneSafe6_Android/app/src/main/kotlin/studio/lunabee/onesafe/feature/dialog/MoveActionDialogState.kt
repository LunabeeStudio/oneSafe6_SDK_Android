package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class MoveActionDialogState(
    destinationName: String?,
    confirmAction: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.move_nextAction_title)

    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.move_dialog_message, destinationName.orEmpty())

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.move_dialog_confirmButton),
            onClick = { confirmAction() },
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
