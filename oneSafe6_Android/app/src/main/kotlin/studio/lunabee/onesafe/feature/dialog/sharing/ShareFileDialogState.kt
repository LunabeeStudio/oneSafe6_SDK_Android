package studio.lunabee.onesafe.feature.dialog.sharing

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class ShareFileDialogState(
    override val dismiss: () -> Unit,
    onCLickOnAcknowledge: () -> Unit,
    onClickOnDoNotRemind: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.share_sendFileWarningDialog_message)

    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.share_sendFileWarningDialog_dontRemindButton),
            clickLabel = LbcTextSpec.StringResource(OSString.share_sendFileWarningDialog_dontRemindButton),
            onClick = onClickOnDoNotRemind,
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.share_sendFileWarningDialog_acknowledgeButton),
            clickLabel = LbcTextSpec.StringResource(OSString.share_sendFileWarningDialog_acknowledgeButton),
            onClick = onCLickOnAcknowledge,
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
