package studio.lunabee.onesafe.commonui.biometric

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.error.OSAppError
import javax.crypto.Cipher

private val logger = LBLogger.get("biometricPrompt")

@Composable
fun biometricPrompt(
    labels: DisplayBiometricLabels,
    getCipher: suspend () -> Cipher?,
    onSuccess: (cipher: Cipher) -> Unit,
    onFailure: (OSAppError) -> Unit = {},
    onUserCancel: () -> Unit = {},
    onNegative: () -> Unit = onUserCancel,
): suspend () -> Unit {
    val context = LocalContext.current
    val title = labels.title.string
    val description = labels.description.string
    val negativeButtonText = labels.negativeButtonText.string
    return suspend {
        getCipher()?.let { cipher ->
            val fragmentActivity: FragmentActivity = context.findFragmentActivity()
            val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setConfirmationRequired(false)
                .build()

            val biometricPrompt = BiometricPrompt(
                fragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED -> onUserCancel()
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onNegative()
                            BiometricPrompt.ERROR_CANCELED -> {
                                // FIXME Ignore error 5 for now. It seems to happen when the devices is unlocked on oneSafe login screen
                                logger.e("Biometric failed. Code $errorCode - $errString")
                                onUserCancel()
                            }
                            else -> onFailure(
                                OSAppError(
                                    code = OSAppError.Code.BIOMETRIC_LOGIN_ERROR,
                                    message = "error $errorCode - $errString",
                                ),
                            )
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        val authenticatedCipher = result.cryptoObject?.cipher
                        if (authenticatedCipher == null) {
                            onFailure(OSAppError(OSAppError.Code.BIOMETRIC_LOGIN_CIPHER_ERROR))
                        } else {
                            onSuccess(authenticatedCipher)
                        }
                    }
                },
            )

            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }
}
