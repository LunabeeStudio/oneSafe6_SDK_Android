package studio.lunabee.onesafe.commonui.snackbar

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec

@Stable
sealed interface SnackbarAction {
    val actionLabel: LbcTextSpec
    val onDismiss: () -> Unit

    class Default(
        val onClick: (() -> Unit),
        override val actionLabel: LbcTextSpec,
        override val onDismiss: () -> Unit,
    ) : SnackbarAction

    class Navigation(
        val route: String,
        override val actionLabel: LbcTextSpec,
        override val onDismiss: () -> Unit,
    ) : SnackbarAction
}
