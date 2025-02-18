package studio.lunabee.onesafe.feature.password.creation

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

@Stable
sealed interface PasswordCreationUiState {
    data class Idle(
        val passwordStrength: LbcTextSpec?,
    ) : PasswordCreationUiState

    data object Error : PasswordCreationUiState {
        val text: LbcTextSpec = LbcTextSpec.StringResource(OSString.changePassword_error_samePasswordAsOtherSafe)
    }

    data object Loading : PasswordCreationUiState
    class Success(
        val reset: (String) -> Unit,
    ) : PasswordCreationUiState
}
