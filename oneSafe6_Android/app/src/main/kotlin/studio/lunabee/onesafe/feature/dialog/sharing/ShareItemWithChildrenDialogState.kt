package studio.lunabee.onesafe.feature.dialog.sharing

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

class ShareItemWithChildrenDialogState(
    override val dismiss: () -> Unit,
    onClickOnIncludeChildren: () -> Unit,
    onClickOnThisItemOnly: () -> Unit,
    subItemCount: Int,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.share_itemWithChildrenDialog_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.share_itemWithChildrenDialog_message)

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.PluralsResource(OSPlurals.share_itemWithChildrenDialog_includeChildrenButton, subItemCount, subItemCount),
            clickLabel = LbcTextSpec.PluralsResource(
                OSPlurals.share_itemWithChildrenDialog_includeChildrenButton,
                subItemCount,
                subItemCount,
            ),
            onClick = onClickOnIncludeChildren,
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.share_itemWithChildrenDialog_onlyItemButton),
            clickLabel = LbcTextSpec.StringResource(OSString.share_itemWithChildrenDialog_onlyItemButton),
            onClick = onClickOnThisItemOnly,
        ),
    )
    override val customContent: (@Composable () -> Unit)? = null
}
