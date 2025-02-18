package studio.lunabee.onesafe.feature.login

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.SplashScreenManager
import studio.lunabee.onesafe.cryptography.android.AndroidKeyStoreEngine
import studio.lunabee.onesafe.cryptography.android.BiometricEngine
import studio.lunabee.onesafe.login.screen.LoginRoute
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import studio.lunabee.onesafe.login.state.LoginUiState
import studio.lunabee.onesafe.login.viewmodel.LoginViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme
import javax.crypto.Cipher

@OptIn(ExperimentalTestApi::class)
class LoginScreenTest : LbcComposeTest() {

    private val onBypass: () -> Unit = spyk({})

    @Test
    fun signin_state_test_biometric_enabled() {
        val vm = mockk<LoginViewModel> {
            every { uiState } returns MutableStateFlow(
                LoginUiState(
                    commonLoginUiState = CommonLoginUiState.Data(
                        currentPasswordValue = TextFieldValue(""),
                        loginResult = CommonLoginUiState.LoginResult.Idle,
                        isFirstLogin = false,
                        isBeta = false,
                    ),
                    biometricCipher = {
                        // real world scenario use getCipherBiometricForDecrypt

                        val cipher = Cipher.getInstance(
                            KeyProperties.KEY_ALGORITHM_AES + "/"
                                + KeyProperties.BLOCK_MODE_CBC + "/"
                                + KeyProperties.ENCRYPTION_PADDING_PKCS7,
                        )
                        val secretKey = AndroidKeyStoreEngine().generateSecretKey(
                            KeyGenParameterSpec.Builder(
                                BiometricEngine.KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                            )
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                                .build(),
                        )
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                        cipher
                    },
                ),
            )
            every { snackbarState } returns MutableStateFlow(null)
        }
        setLoginRoute(vm) {
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitUntilExactlyOneExists()
            hasTestTag(UiConstants.TestTag.Item.LoginBiometricIcon)
                .waitUntilExactlyOneExists()
        }
    }

    @Test
    fun signin_state_test_biometric_disabled() {
        val vm = mockk<LoginViewModel>()
        every { vm.uiState } returns MutableStateFlow(
            LoginUiState(
                CommonLoginUiState.Data(
                    currentPasswordValue = TextFieldValue(""),
                    loginResult = CommonLoginUiState.LoginResult.Idle,
                    isFirstLogin = false,
                    isBeta = false,
                ),
                biometricCipher = null,
            ),
        )
        every { vm.snackbarState } returns MutableStateFlow(null)
        setLoginRoute(vm) {
            onRoot().printToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Login}")
            onNodeWithTag(UiConstants.TestTag.Screen.Login).assertExists()
            onNodeWithTag(UiConstants.TestTag.Item.LoginBiometricIcon).assertDoesNotExist()
            onNodeWithText(getString(OSString.signInScreen_form_password_error)).assertDoesNotExist()
        }
    }

    @Test
    fun signin_state_test_error() {
        val vm = mockk<LoginViewModel>()
        every { vm.uiState } returns MutableStateFlow(
            LoginUiState(
                CommonLoginUiState.Data(
                    currentPasswordValue = TextFieldValue(""),
                    loginResult = CommonLoginUiState.LoginResult.Error,
                    isFirstLogin = false,
                    isBeta = false,
                ),
                null,
            ),
        )
        every { vm.snackbarState } returns MutableStateFlow(null)
        setLoginRoute(vm) {
            onRoot().printToCacheDir(printRule, UiConstants.TestTag.Screen.Login)
            onNodeWithTag(UiConstants.TestTag.Screen.Login).assertExists()
            hasText(getString(OSString.signInScreen_form_password_error))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun bypass_state_error() {
        val vm = mockk<LoginViewModel>()
        every { vm.uiState } returns MutableStateFlow(LoginUiState(CommonLoginUiState.Bypass, null))
        every { vm.snackbarState } returns MutableStateFlow(null)
        setLoginRoute(vm) {
            hasTestTag(UiConstants.TestTag.Screen.Login).waitUntilDoesNotExist()
        }
        verify(exactly = 1) { onBypass() }
    }

    @Test
    fun home_item_appBetaVersion_test() {
        val uiStateFlow = MutableStateFlow(
            LoginUiState(
                CommonLoginUiState.Data(
                    currentPasswordValue = TextFieldValue(""),
                    loginResult = CommonLoginUiState.LoginResult.Idle,
                    isFirstLogin = false,
                    isBeta = true,
                ),
                null,
            ),
        )
        val vm = mockk<LoginViewModel> {
            every { uiState } returns uiStateFlow
            every { snackbarState } returns MutableStateFlow(null)
        }
        setLoginRoute(vm) {
            hasText(getString(OSString.appBetaVersion_chip))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            uiStateFlow.value = LoginUiState(
                CommonLoginUiState.Data(
                    currentPasswordValue = TextFieldValue(""),
                    loginResult = CommonLoginUiState.LoginResult.Idle,
                    isFirstLogin = false,
                    isBeta = false,
                ),
                null,
            )

            hasText(getString(OSString.appBetaVersion_chip))
                .waitUntilDoesNotExist()
        }
    }

    // Use FragmentActivity to allow biometric prompt
    private fun setLoginRoute(viewModel: LoginViewModel, block: AndroidComposeUiTest<FragmentActivity>.() -> Unit) {
        every { viewModel.versionName } returns "1.11.0"
        every { viewModel.splashScreenManager } returns SplashScreenManager()
        invoke(FragmentActivity::class.java) {
            setContent {
                OSTheme {
                    LoginRoute(onSuccess = { }, viewModel = viewModel, onBypass = onBypass, onCreateNewSafe = { })
                }
            }
            block()
        }
    }
}
