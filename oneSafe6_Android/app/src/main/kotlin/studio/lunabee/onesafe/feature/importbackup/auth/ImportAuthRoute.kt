package studio.lunabee.onesafe.feature.importbackup.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog

@Composable
fun ImportAuthRoute(
    navigateBack: () -> Unit,
    onSuccess: (hasBubblesData: Boolean) -> Unit,
    viewModel: ImportAuthViewModel = hiltViewModel(),
) {
    val importAuthState: ImportAuthState by viewModel.importAuthState.collectAsStateWithLifecycle()

    LoadingBackHandler(enabled = importAuthState !is ImportAuthState.AuthInProgress, onBack = navigateBack)

    when (val state = importAuthState) {
        is ImportAuthState.AuthInProgress,
        is ImportAuthState.WaitingForUserInput,
        is ImportAuthState.WrongCredentials,
        -> {
            // No-op: handle separately
        }
        is ImportAuthState.UnexpectedError -> state.dialogState.DefaultAlertDialog()
        is ImportAuthState.Success -> {
            onSuccess(state.doesArchiveContainsBubblesData)
            state.reset()
        }
    }

    ImportAuthScreen(
        unlockArchive = viewModel::unlockArchive,
        displayCredentialsError = importAuthState is ImportAuthState.WrongCredentials,
        resetCredentialsError = viewModel::waitForUserInput,
        isCheckingPassword = importAuthState is ImportAuthState.AuthInProgress,
        navigateBackToSettings = navigateBack,
        importAuthArchiveKindLabels = viewModel.importAuthArchiveKindLabels,
    )
}
