package studio.lunabee.onesafe.feature.settings

import javax.crypto.Cipher

sealed interface SettingsUiState {
    data object Initializing : SettingsUiState
    data object Idle : SettingsUiState
    data object ShowPasswordAuthentication : SettingsUiState
    class ShowBiometricAuthentication(val cipher: Cipher) : SettingsUiState
    class NavigateChangePassword(val reset: () -> Unit) : SettingsUiState
}
