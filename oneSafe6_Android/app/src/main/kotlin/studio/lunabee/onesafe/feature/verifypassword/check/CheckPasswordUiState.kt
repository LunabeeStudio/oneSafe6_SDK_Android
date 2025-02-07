package studio.lunabee.onesafe.feature.verifypassword.check

import androidx.compose.runtime.Stable

@Stable
interface CheckPasswordUiState {
    object RightPassword : CheckPasswordUiState
    object WrongPassword : CheckPasswordUiState
    object Loading : CheckPasswordUiState
    object Idle : CheckPasswordUiState
}
