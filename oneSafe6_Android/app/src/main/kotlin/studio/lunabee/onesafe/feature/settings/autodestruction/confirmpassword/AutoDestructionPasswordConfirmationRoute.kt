package studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword

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
fun AutoDestructionPasswordConfirmationRoute(
    navigateBack: () -> Unit,
    navigateBackToSettings: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    viewModel: AutoDestructionPasswordConfirmationViewModel = hiltViewModel(),
) {
    val uiState: AutoDestructionPasswordConfirmationUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember {
        derivedStateOf {
            passwordValue.isNotBlank() && uiState == AutoDestructionPasswordConfirmationUiState.Idle
        }
    }

    (uiState as? AutoDestructionPasswordConfirmationUiState.Exit)?.let { state ->
        val snackbarVisuals = state.snackbarState?.snackbarVisuals
        LaunchedEffect(Unit) {
            snackbarVisuals?.let { showSnackBar(it) }
            navigateBackToSettings()
        }
    }

    dialogState?.DefaultAlertDialog()

    PasswordConfirmationScreen(
        labels = PasswordConfirmationScreenLabels.AutoDestruction,
        navigateBack = navigateBack,
        confirmClick = { viewModel.checkPassword(passwordValue) },
        isConfirmEnabled = isConfirmEnabled,
        passwordValue = passwordValue,
        onValueChange = {
            passwordValue = it
            (uiState as? AutoDestructionPasswordConfirmationUiState.FieldError)?.reset?.invoke()
        },
        isLoading = false,
        fieldError = (uiState as? AutoDestructionPasswordConfirmationUiState.FieldError)?.error,
    )
}
