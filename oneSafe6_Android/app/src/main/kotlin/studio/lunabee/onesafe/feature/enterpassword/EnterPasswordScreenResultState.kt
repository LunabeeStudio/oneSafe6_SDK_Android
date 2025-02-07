package studio.lunabee.onesafe.feature.enterpassword

import androidx.compose.runtime.Stable

@Stable
data class EnterPasswordUiState(
    val password: String = "",
    val screenResult: EnterPasswordScreenResultState = EnterPasswordScreenResultState.Idle,
)

enum class EnterPasswordScreenResultState {
    Idle,
    Loading,
    Success,
    Error,
}
