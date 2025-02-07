package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class OverEncryptionEnabledConfirmDialogState(
    override val dismiss: () -> Unit,
    onConfirm: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_cancel),
            clickLabel = null,
            onClick = dismiss,
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_yesDisable),
            clickLabel = null,
            onClick = {
                onConfirm()
                dismiss()
            },
        ),
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.overEncryptionEnabled_confirmDialog_message)
    override val customContent: @Composable (() -> Unit)? = null
}
