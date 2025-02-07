package studio.lunabee.onesafe.commonui.notification

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class NotificationPermissionRationaleDialogState(
    launchPermissionRequest: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonOk {
            launchPermissionRequest()
            dismiss()
        },
    )

    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.notification_permission_rationale_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.notification_permission_rationale_message)
    override val customContent: (@Composable () -> Unit)? = null
}
