package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.runtime.Stable

@Stable
sealed interface OverEncryptionEnabledUiState {
    data class Idle(
        val isBackupEnabled: Boolean,
    ) : OverEncryptionEnabledUiState

    data class Loading(val step: Step) : OverEncryptionEnabledUiState {
        enum class Step {
            Backup, Decryption
        }
    }

    data object Done : OverEncryptionEnabledUiState
    data class Error(
        val error: Throwable?,
    ) : OverEncryptionEnabledUiState
}
