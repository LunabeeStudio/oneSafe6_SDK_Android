package studio.lunabee.onesafe.feature.importbackup.savedata

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.snackbar.DefaultSnackbarVisuals
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind

@Composable
fun ImportSaveDataRoute(
    navigateBackToFileSelection: () -> Unit,
    navigateBackToSettings: () -> Unit,
    showSnackBar: ((SnackbarVisuals) -> Unit)? = null,
    viewModel: ImportSaveDataViewModel = hiltViewModel(),
) {
    val importSaveDataState: ImportSaveDataUiState by viewModel.importSaveDataState.collectAsStateWithLifecycle()
    val currentSafeItemCount: Int by viewModel.currentSafeItemCount.collectAsStateWithLifecycle()

    LoadingBackHandler(enabled = importSaveDataState !is ImportSaveDataUiState.ImportInProgress) {
        navigateBackToSettings()
    }

    val successMessage = stringResource(id = OSString.import_success_message)
    when (val state = importSaveDataState) {
        is ImportSaveDataUiState.Dialog -> state.dialogState.DefaultAlertDialog()
        is ImportSaveDataUiState.ExitWithError -> {
            val snackbarVisuals = state.error?.snackbarVisuals ?: DefaultSnackbarVisuals(
                message = stringResource(id = OSString.error_defaultMessage),
                withDismissAction = true,
                duration = SnackbarDuration.Short,
                actionLabel = null,
            )
            LaunchedEffect(Unit) {
                showSnackBar?.invoke(snackbarVisuals)
                navigateBackToFileSelection()
                viewModel.resetState()
            }
        }
        ImportSaveDataUiState.Success -> {
            LaunchedEffect(Unit) {
                showSnackBar?.invoke(
                    DefaultSnackbarVisuals(
                        message = successMessage,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                        actionLabel = null,
                    ),
                )
                navigateBackToSettings()
            }
        }
        is ImportSaveDataUiState.ImportInProgress,
        is ImportSaveDataUiState.WaitingForUserChoice,
        -> {
            // Handle directly in screen
        }
    }

    when (viewModel.archiveKind) {
        OSArchiveKind.Backup -> {
            ImportSaveDataScreen(
                itemCount = viewModel.metadataResult.data?.itemCount,
                importSaveDataState = importSaveDataState,
                navigateBack = navigateBackToSettings,
                startImport = viewModel::startImport,
                currentSafeItemCount = currentSafeItemCount,
            )
        }
        OSArchiveKind.Sharing -> {
            SharingSaveDataScreen(
                itemCount = viewModel.metadataResult.data?.itemCount,
                importSaveDataState = importSaveDataState,
                navigateBack = navigateBackToSettings,
            )
        }
        OSArchiveKind.Unknown -> {}
    }
}
