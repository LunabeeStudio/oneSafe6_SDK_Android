package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Stable

@Stable
sealed interface OverEncryptionKeyUiState {
    data class Idle(
        val key: String,
    ) : OverEncryptionKeyUiState

    data class Loading(val step: Step) : OverEncryptionKeyUiState {
        enum class Step {
            Backup, Encryption
        }
    }

    data object Done : OverEncryptionKeyUiState
    data class Error(
        val error: Throwable?,
    ) : OverEncryptionKeyUiState
}
