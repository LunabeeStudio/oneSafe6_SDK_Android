package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class DuplicateItemDialogState(
    itemName: OSNameProvider,
    parentName: OSNameProvider?,
    duplicate: () -> Unit,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(id = OSString.safeItemDetail_duplicateDialog_title)
    override val message: LbcTextSpec = if (parentName != null) {
        LbcTextSpec.StringResource(
            id = OSString.safeItemDetail_duplicateDialog_childrenMessage,
            itemName.name,
            parentName.name,
        )
    } else {
        LbcTextSpec.StringResource(id = OSString.safeItemDetail_duplicateDialog_rootMessage, itemName.name)
    }
    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(LbcTextSpec.StringResource(id = OSString.safeItemDetail_duplicateDialog_ok), onClick = duplicate),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
