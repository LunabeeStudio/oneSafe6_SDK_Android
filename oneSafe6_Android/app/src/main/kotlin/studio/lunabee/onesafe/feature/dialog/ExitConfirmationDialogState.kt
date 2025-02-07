package studio.lunabee.onesafe.feature.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
class ExitConfirmationDialogState(
    override val actions: List<DialogAction>,
    override val dismiss: () -> Unit,
) : DialogState {
    override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_cancelAlert_title)
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_cancelAlert_message)

    companion object {
        fun getState(
            dismiss: () -> Unit,
            navigateBack: () -> Unit,
        ): ExitConfirmationDialogState {
            return ExitConfirmationDialogState(
                actions = listOf(
                    DialogAction.commonCancel(onClick = dismiss),
                    DialogAction(
                        text = LbcTextSpec.StringResource(id = OSString.safeItemDetail_cancelAlert_close),
                        clickLabel = null, // button text is enough
                        onClick = {
                            dismiss()
                            navigateBack()
                        },
                    ),
                ),
                dismiss = dismiss,
            )
        }
    }

    override val customContent: (@Composable () -> Unit)? = null
}
