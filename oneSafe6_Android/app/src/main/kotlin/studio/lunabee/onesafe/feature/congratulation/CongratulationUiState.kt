package studio.lunabee.onesafe.feature.congratulation

sealed interface CongratulationUiState {
    data object Finishing : CongratulationUiState
    data class Idle(
        val isLoggedIn: Boolean,
    ) : CongratulationUiState
}
