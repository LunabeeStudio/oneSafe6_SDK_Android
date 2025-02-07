package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class ColorConfirmationDialogState(
    onColorConfirmation: (hasConfirm: Boolean) -> Unit,
) : DialogState {
    override val actions: List<DialogAction> = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_no),
            clickLabel = null,
            onClick = {
                onColorConfirmation(false)
            },
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(id = OSString.common_yes),
            clickLabel = null,
            onClick = {
                onColorConfirmation(true)
            },
        ),
    )
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_colorExtract_confirmation_message)
    override val dismiss: () -> Unit = { onColorConfirmation(false) }
    override val customContent: (@Composable () -> Unit)? = null
}
