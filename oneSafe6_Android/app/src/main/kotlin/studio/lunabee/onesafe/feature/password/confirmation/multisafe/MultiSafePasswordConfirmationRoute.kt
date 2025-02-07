package studio.lunabee.onesafe.feature.password.confirmation.multisafe

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
fun MultiSafePasswordConfirmationRoute(
    navigateBack: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: MultiSafePasswordConfirmationViewModel = hiltViewModel(),
) {
    val uiState: MultiSafePasswordConfirmationUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember {
        derivedStateOf {
            passwordValue.isNotBlank() && uiState == MultiSafePasswordConfirmationUiState.Idle
        }
    }

    (uiState as? MultiSafePasswordConfirmationUiState.Success)?.let { state ->
        LaunchedEffect(Unit) {
            onConfirm()
            state.reset()
        }
    }

    dialogState?.DefaultAlertDialog()

    PasswordConfirmationScreen(
        labels = PasswordConfirmationScreenLabels.MultiSafe,
        navigateBack = navigateBack,
        confirmClick = { viewModel.checkPassword(passwordValue) },
        isConfirmEnabled = isConfirmEnabled,
        passwordValue = passwordValue,
        onValueChange = {
            passwordValue = it
            (uiState as? MultiSafePasswordConfirmationUiState.FieldError)?.reset?.invoke()
        },
        isLoading = uiState == MultiSafePasswordConfirmationUiState.Loading,
        fieldError = (uiState as? MultiSafePasswordConfirmationUiState.FieldError)?.error,
    )
}
