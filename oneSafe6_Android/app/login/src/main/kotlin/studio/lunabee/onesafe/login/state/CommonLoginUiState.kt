package studio.lunabee.onesafe.login.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
sealed interface CommonLoginUiState {
    data object Initialize : CommonLoginUiState
    data object Bypass : CommonLoginUiState

    data class Data(
        val currentPasswordValue: TextFieldValue,
        val loginResult: LoginResult,
        val isFirstLogin: Boolean,
        val isBeta: Boolean,
    ) : CommonLoginUiState

    sealed interface LoginResult {
        data object Idle : LoginResult
        data object Error : LoginResult
        data class Success(val restoreState: Boolean) : LoginResult
        data object Loading : LoginResult
    }
}
