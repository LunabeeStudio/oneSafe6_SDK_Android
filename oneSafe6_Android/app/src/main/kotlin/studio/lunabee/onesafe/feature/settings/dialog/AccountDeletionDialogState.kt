package studio.lunabee.onesafe.feature.settings.dialog

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class AccountDeletionDialogState(
    override val dismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.settings_section_safeAction_accountDeletion_alert_message)

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.settings_section_safeAction_accountDeletion_alert_confirm),
            clickLabel = LbcTextSpec.StringResource(OSString.settings_section_safeAction_accountDeletion_alert_confirm),
            onClick = onDeleteClick,
            type = DialogAction.Type.Dangerous,
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
