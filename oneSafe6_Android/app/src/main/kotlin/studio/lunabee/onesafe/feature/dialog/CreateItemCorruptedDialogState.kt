package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class CreateItemCorruptedDialogState(
    override val actions: List<DialogAction>,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.error_defaultTitle)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_corrupted_addItemMessage)
    override val customContent: (@Composable () -> Unit)? = null
}
