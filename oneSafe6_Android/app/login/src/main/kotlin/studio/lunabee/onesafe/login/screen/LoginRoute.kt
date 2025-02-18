package studio.lunabee.onesafe.login.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.commonui.extension.safeRequestFocus
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.utils.KeyboardVisibility
import studio.lunabee.onesafe.commonui.utils.rememberKeyboardVisibility
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import studio.lunabee.onesafe.login.state.LoginUiState
import studio.lunabee.onesafe.login.viewmodel.LoginViewModel
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun LoginRoute(
    onSuccess: (restoreState: Boolean) -> Unit,
    onBypass: () -> Unit = { onSuccess(true) },
    viewModel: LoginViewModel = hiltViewModel(),
    onCreateNewSafe: () -> Unit,
) {
    val loginUiState: LoginUiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val uiState = loginUiState.commonLoginUiState) {
        CommonLoginUiState.Initialize -> {
            Box(Modifier.fillMaxSize())
        }
        CommonLoginUiState.Bypass -> {
            Box(Modifier.fillMaxSize())
            LaunchedEffect(Unit) {
                onBypass()
                viewModel.splashScreenManager.isAppReady = true
            }
        }
        is CommonLoginUiState.Data -> {
            LaunchedEffect(Unit) {
                viewModel.splashScreenManager.isAppReady = true
            }
            val vmSnackbarState by viewModel.snackbarState.collectAsStateWithLifecycle(null)
            val exitIcon: LoginExitIcon = LoginExitIcon.None
            val focusManager: FocusManager = LocalFocusManager.current
            val focusRequester: FocusRequester = remember { FocusRequester() }
            val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
            val alreadyRequestedBiometric = rememberSaveable { mutableStateOf(false) }

            val keyboardState: KeyboardVisibility by rememberKeyboardVisibility()
            val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
            val coroutineScope = rememberCoroutineScope()

            val loginResult = uiState.loginResult
            val isKeyboardVisible: Boolean = keyboardState == KeyboardVisibility.Visible
            LaunchedEffect(key1 = loginResult is CommonLoginUiState.LoginResult.Success, key2 = isKeyboardVisible) {
                if (loginResult is CommonLoginUiState.LoginResult.Success && isKeyboardVisible) {
                    keyboardController?.hide()
                } else if (loginResult is CommonLoginUiState.LoginResult.Success) {
                    onSuccess(loginResult.restoreState)
                }
            }

            val showLocalSnackBar: suspend (visuals: SnackbarVisuals) -> Unit = { snackbarVisuals ->
                snackbarHostState.showSnackbar(snackbarVisuals)
            }

            var errorSnackbarState: ErrorSnackbarState? by remember {
                mutableStateOf(null)
            }
            (vmSnackbarState ?: errorSnackbarState)?.snackbarVisuals?.let {
                LaunchedEffect(it) {
                    showLocalSnackBar(it)
                }
            }

            val onBiometricClick = loginUiState.biometricCipher?.let { getBiometricCipher ->
                val biometricPrompt: suspend () -> Unit = biometricPrompt(
                    labels = DisplayBiometricLabels.Login,
                    getCipher = getBiometricCipher,
                    onSuccess = viewModel::biometricLogin,
                    onFailure = { error ->
                        errorSnackbarState = ErrorSnackbarState(error = error, onClick = {
                            errorSnackbarState = null
                        })
                    },
                    onUserCancel = {
                        focusRequester.safeRequestFocus(coroutineScope)
                    },
                    onNegative = {
                        focusRequester.safeRequestFocus(coroutineScope)
                    },
                )

                if (!alreadyRequestedBiometric.value) {
                    LaunchedEffect(Unit) {
                        alreadyRequestedBiometric.value = true
                        focusManager.clearFocus(true)
                        biometricPrompt()
                    }
                }

                fun() {
                    focusManager.clearFocus(true)
                    snackbarHostState.currentSnackbarData?.dismiss()
                    coroutineScope.launch {
                        biometricPrompt()
                    }
                }
            }

            OSTheme(
                isMaterialYouSettingsEnabled = false,
            ) {
                LoginScreenWrapper(
                    exitIcon = exitIcon,
                    uiState = uiState,
                    setPasswordValue = viewModel::setPasswordValue,
                    versionName = viewModel.versionName,
                    loginFromPassword = viewModel::loginFromPassword,
                    onBiometricClick = onBiometricClick,
                    logoRes = OSDrawable.ic_onesafe_text,
                    focusRequester = focusRequester,
                    snackbarHostState = snackbarHostState,
                    isIllustrationDisplayed = true,
                    onCreateNewSafe = onCreateNewSafe,
                )
            }
        }
    }
}
