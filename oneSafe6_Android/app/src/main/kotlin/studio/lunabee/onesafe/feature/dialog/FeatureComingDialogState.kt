package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class FeatureComingDialogState(override val dismiss: () -> Unit) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(LbcTextSpec.StringResource(OSString.home_addItem_upcomingAlert_dismissButton), onClick = dismiss),
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.home_addItem_upcomingAlert_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.home_addItem_upcomingAlert_message)
    override val customContent: (@Composable () -> Unit)? = null
}
