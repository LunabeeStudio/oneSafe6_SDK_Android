package studio.lunabee.onesafe.login.state

import androidx.compose.runtime.Stable
import javax.crypto.Cipher

@Stable
data class LoginUiState(
    val commonLoginUiState: CommonLoginUiState,
    val biometricCipher: (suspend () -> Cipher?)?,
)
