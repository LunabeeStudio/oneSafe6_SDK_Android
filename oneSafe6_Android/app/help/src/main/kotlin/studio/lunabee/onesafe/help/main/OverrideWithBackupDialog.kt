package studio.lunabee.onesafe.help.main

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.dialog.OSDefaultAlertDialog

@Composable
internal fun OverrideWithBackupDialog(onValid: () -> Unit, onCancel: () -> Unit) {
    OSDefaultAlertDialog(
        onDismissRequest = { onCancel() },
        title = LbcTextSpec.StringResource(OSString.help_overrideBackupDialog_title),
        message = LbcTextSpec.StringResource(OSString.help_overrideBackupDialog_message),
        actionContent = {
            DialogAction.commonCancel(onCancel).ActionButton()
            DialogAction(
                text = LbcTextSpec.StringResource(id = OSString.common_restore),
                type = DialogAction.Type.Normal,
                onClick = onValid,
                clickLabel = LbcTextSpec.StringResource(id = OSString.common_restore),
            ).ActionButton()
        },
    )
}
