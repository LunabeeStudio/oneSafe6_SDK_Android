package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class WrongExtensionDialogState(
    retry: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.error_defaultTitle)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.import_selectFile_error_wrongFileSelected)
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.import_selectFile_error_selectAnotherOne),
            onClick = retry,
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
