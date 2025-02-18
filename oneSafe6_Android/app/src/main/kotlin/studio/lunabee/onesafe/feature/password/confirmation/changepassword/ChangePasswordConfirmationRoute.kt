package studio.lunabee.onesafe.feature.password.confirmation.changepassword

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreen
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreenLabels

@Composable
fun ChangePasswordConfirmationRoute(
    navigateBack: () -> Unit,
    navigateNext: (hasBiometric: Boolean) -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    viewModel: ChangePasswordConfirmationViewModel = hiltViewModel(),
) {
    val uiState: ChangePasswordConfirmationUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember {
        derivedStateOf {
            passwordValue.isNotBlank() && uiState == ChangePasswordConfirmationUiState.Idle
        }
    }

    (uiState as? ChangePasswordConfirmationUiState.Exit)?.let { state ->
        val snackbarVisuals = state.snackbarState?.snackbarVisuals
        LaunchedEffect(Unit) {
            snackbarVisuals?.let { showSnackBar(it) }
            navigateNext(state.isBiometricEnabled)
        }
    }

    dialogState?.DefaultAlertDialog()

    PasswordConfirmationScreen(
        labels = PasswordConfirmationScreenLabels.ChangePassword,
        navigateBack = navigateBack,
        confirmClick = { viewModel.checkPassword(passwordValue) },
        isConfirmEnabled = isConfirmEnabled,
        passwordValue = passwordValue,
        onValueChange = {
            passwordValue = it
            (uiState as? ChangePasswordConfirmationUiState.FieldError)?.reset?.invoke()
        },
        isLoading = uiState == ChangePasswordConfirmationUiState.Loading,
        fieldError = (uiState as? ChangePasswordConfirmationUiState.FieldError)?.error,
    )
}
