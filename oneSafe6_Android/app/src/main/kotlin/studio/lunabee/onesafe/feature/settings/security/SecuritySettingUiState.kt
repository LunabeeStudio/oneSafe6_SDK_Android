package studio.lunabee.onesafe.feature.settings.security

import studio.lunabee.onesafe.error.OSError

data class SecuritySettingUiState(
    val screenResult: ScreenResult = ScreenResult.Idle,
) {
    sealed interface ScreenResult {
        object Idle : ScreenResult
        data class Error(
            val osError: OSError,
        ) : ScreenResult
    }
}
