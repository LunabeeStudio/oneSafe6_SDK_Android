package studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

@Stable
sealed interface AutoDestructionPasswordConfirmationUiState {
    data object Idle : AutoDestructionPasswordConfirmationUiState
    class Exit(
        val snackbarState: SnackbarState?,
    ) : AutoDestructionPasswordConfirmationUiState

    class FieldError(
        val error: OSError,
        val reset: () -> Unit,
    ) : AutoDestructionPasswordConfirmationUiState
}
