package studio.lunabee.onesafe.help.cipherkeyprompt

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec

@Stable
data class CipherKeyPromptUiState(
    val key: String,
    val openDatabaseResult: OpenDatabaseState,
) {

    sealed interface OpenDatabaseState {
        data object Idle : OpenDatabaseState
        data class Error(val description: LbcTextSpec?) : OpenDatabaseState
        data object Success : OpenDatabaseState
        data object Loading : OpenDatabaseState
    }

    companion object {
        fun default(): CipherKeyPromptUiState {
            return CipherKeyPromptUiState("", OpenDatabaseState.Idle)
        }
    }
}
