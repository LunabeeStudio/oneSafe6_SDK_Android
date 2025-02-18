package studio.lunabee.onesafe.feature.itemform.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.Constant

@Stable
class FileToLargeDialogState(
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.error_file_tooBig_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.error_file_tooBig_message, Constant.FileMaxSizeMegaBytes)
    override val actions: List<DialogAction> = listOf(DialogAction.commonOk(dismiss))
    override val customContent: (@Composable () -> Unit)? = null
}
