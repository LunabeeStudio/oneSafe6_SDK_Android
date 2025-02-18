package studio.lunabee.onesafe.feature.verifypassword.check

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
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreen
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreenLabels

@Composable
fun CheckPasswordRoute(
    navigateBack: () -> Unit,
    navigateToRightPassword: () -> Unit,
    navigateToWrongPassword: () -> Unit,
    viewModel: CheckPasswordViewModel = hiltViewModel(),
) {
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember { derivedStateOf { passwordValue.isNotBlank() } }
    val uiState: CheckPasswordUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState: DialogState? by viewModel.dialogState.collectAsStateWithLifecycle()

    when (uiState) {
        CheckPasswordUiState.RightPassword -> {
            LaunchedEffect(Unit) {
                navigateToRightPassword()
                viewModel.resetState()
            }
        }
        CheckPasswordUiState.WrongPassword -> {
            LaunchedEffect(Unit) {
                navigateToWrongPassword()
                viewModel.resetState()
            }
        }
    }

    PasswordConfirmationScreen(
        labels = PasswordConfirmationScreenLabels.VerifyPassword,
        navigateBack = navigateBack,
        confirmClick = { viewModel.checkPassword(passwordValue) },
        isConfirmEnabled = isConfirmEnabled,
        passwordValue = passwordValue,
        onValueChange = { passwordValue = it },
        isLoading = uiState == CheckPasswordUiState.Loading,
        fieldError = null,
    )
    dialogState?.DefaultAlertDialog()
}
