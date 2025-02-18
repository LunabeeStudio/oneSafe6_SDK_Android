package studio.lunabee.onesafe.commonui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.error.title

@Stable
open class ErrorDialogState(
    error: Throwable?,
    override val actions: List<DialogAction>,
    override val dismiss: () -> Unit = {},
) : DialogState {
    override val title: LbcTextSpec = error.title()
    override val message: LbcTextSpec = error.description()
    override val customContent: (@Composable () -> Unit)? = null
}
