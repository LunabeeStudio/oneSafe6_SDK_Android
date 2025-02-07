package studio.lunabee.onesafe.feature.verifypassword.wrongpassword

import androidx.compose.runtime.Stable
import javax.crypto.Cipher

@Stable
interface WrongPasswordUiState {
    data class ShowBiometric(val cipher: Cipher) : WrongPasswordUiState
    object Idle : WrongPasswordUiState
}
