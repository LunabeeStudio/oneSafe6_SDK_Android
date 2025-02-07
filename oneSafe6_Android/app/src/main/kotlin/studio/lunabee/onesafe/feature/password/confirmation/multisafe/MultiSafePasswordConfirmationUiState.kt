package studio.lunabee.onesafe.feature.password.confirmation.multisafe

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.error.OSError

@Stable
sealed interface MultiSafePasswordConfirmationUiState {
    data object Idle : MultiSafePasswordConfirmationUiState

    data object Loading : MultiSafePasswordConfirmationUiState

    data class Success(
        val reset: () -> Unit,
    ) : MultiSafePasswordConfirmationUiState

    data class Exit(
        val snackbarState: SnackbarState?,
        val isBiometricEnabled: Boolean,
    ) : MultiSafePasswordConfirmationUiState

    data class FieldError(
        val error: OSError,
        val reset: () -> Unit,
    ) : MultiSafePasswordConfirmationUiState
}
