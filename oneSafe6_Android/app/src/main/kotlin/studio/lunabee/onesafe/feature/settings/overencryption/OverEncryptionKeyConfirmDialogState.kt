package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class OverEncryptionKeyConfirmDialogState(
    override val dismiss: () -> Unit,
    onConfirm: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_noCancel),
            clickLabel = null,
            onClick = dismiss,
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_yesLetsGo),
            clickLabel = null,
            onClick = {
                onConfirm()
                dismiss()
            },
        ),
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.overEncryptionKey_confirmationDialog_message)
    override val customContent: @Composable (() -> Unit)? = null
}
