package studio.lunabee.onesafe.commonui.dialog

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

class OverEncryptionCanceledDialogState(
    onClose: () -> Unit,
    onOpenDiscord: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_button_discord),
            onClick = onOpenDiscord,
        ),
        DialogAction(LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_button_login), onClick = onClose),
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_message)
    override val dismiss: () -> Unit = {}
    override val customContent: @Composable (() -> Unit)? = null
}
