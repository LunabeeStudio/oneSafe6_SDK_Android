package studio.lunabee.onesafe.feature.password.confirmation.onboarding

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.error.OSError

@Stable
sealed interface OnboardingPasswordConfirmationUiState {
    data object Idle : OnboardingPasswordConfirmationUiState
    data object Loading : OnboardingPasswordConfirmationUiState
    class Success(
        val reset: () -> Unit,
    ) : OnboardingPasswordConfirmationUiState

    class FieldError(
        val error: OSError,
        val reset: () -> Unit,
    ) : OnboardingPasswordConfirmationUiState
}
