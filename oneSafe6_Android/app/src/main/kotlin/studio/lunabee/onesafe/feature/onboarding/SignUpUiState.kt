package studio.lunabee.onesafe.feature.onboarding

import studio.lunabee.onesafe.error.OSError

sealed interface SignUpUiState {
    data object Idle : SignUpUiState
    data class Error(
        val error: OSError,
    ) : SignUpUiState

    data object Success : SignUpUiState
}
