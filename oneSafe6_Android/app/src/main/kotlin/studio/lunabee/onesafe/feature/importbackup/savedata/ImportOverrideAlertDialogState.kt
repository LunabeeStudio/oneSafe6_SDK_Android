package studio.lunabee.onesafe.feature.importbackup.savedata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class ImportOverrideAlertDialogState(
    launchImport: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(text = LbcTextSpec.StringResource(id = OSString.safeItemDetail_cancelAlert_yes)) {
            launchImport()
        },
    )

    override val title: LbcTextSpec =
        LbcTextSpec.StringResource(id = OSString.importSettings_overrideDialog_title)
    override val message: LbcTextSpec =
        LbcTextSpec.StringResource(id = OSString.importSettings_overrideDialog_message)
    override val customContent: (@Composable () -> Unit)? = null
}
