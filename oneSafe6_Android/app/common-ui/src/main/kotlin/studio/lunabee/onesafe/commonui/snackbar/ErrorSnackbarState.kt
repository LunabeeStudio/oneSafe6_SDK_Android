package studio.lunabee.onesafe.commonui.snackbar

import androidx.compose.material3.SnackbarDuration
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.error.title

class ErrorSnackbarState(
    override val message: LbcTextSpec,
    onClick: () -> Unit,
) : SnackbarState(
    SnackbarAction.Default(
        onClick = onClick,
        actionLabel = LbcTextSpec.StringResource(OSString.common_ok),
        onDismiss = {},
    ),
) {
    constructor(error: Throwable?, onClick: () -> Unit) : this(LbcTextSpec.Raw("%s\n%s", error.title(), error.description()), onClick)

    override val duration: SnackbarDuration = SnackbarDuration.Indefinite
}
