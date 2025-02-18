package studio.lunabee.onesafe.feature.password.confirmation.changepassword

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

@Stable
sealed interface ChangePasswordConfirmationUiState {
    data object Idle : ChangePasswordConfirmationUiState
    data object Loading : ChangePasswordConfirmationUiState
    class Exit(
        val snackbarState: SnackbarState?,
        val isBiometricEnabled: Boolean,
    ) : ChangePasswordConfirmationUiState

    class FieldError(
        val error: OSError,
        val reset: () -> Unit,
    ) : ChangePasswordConfirmationUiState
}
