package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class DeleteItemDialogState(
    itemNameProvider: OSNameProvider,
    deleteAction: () -> Unit,
    override val dismiss: () -> Unit,
    isCorrupted: Boolean,
    childrenCount: Int,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_delete_alert_title)
    override val message: LbcTextSpec = when {
        isCorrupted && childrenCount > 0 -> LbcTextSpec.PluralsResource(
            id = OSPlurals.safeItemDetail_delete_alert_message_corruptedItemWithChildren,
            count = childrenCount,
            childrenCount,
        )
        isCorrupted -> LbcTextSpec.StringResource(OSString.safeItemDetail_delete_alert_message_corruptedWithoutChildren)
        childrenCount > 0 -> LbcTextSpec.PluralsResource(
            id = OSPlurals.safeItemDetail_delete_alert_message_itemWithChildren,
            count = childrenCount,
            childrenCount,
            itemNameProvider.truncatedName,
        )
        else -> LbcTextSpec.StringResource(
            id = OSString.safeItemDetail_delete_alert_message_withoutChildren,
            itemNameProvider.truncatedName,
        )
    }

    override val actions: List<DialogAction> = listOf(
        DialogAction.commonCancel(dismiss),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.safeItemDetail_delete_alert_button_confirm),
            type = DialogAction.Type.Dangerous,
            onClick = deleteAction,
        ),
    )

    override val customContent: (@Composable () -> Unit)? = null
}
