package studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class OverridePasswordDialogState(
    onConfirm: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {

    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.passwordGenerator_overrideDialog_message)

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.passwordGenerator_overrideDialog_confirmButton),
            type = DialogAction.Type.Normal,
            onClick = onConfirm,
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
