package studio.lunabee.onesafe.feature.exportbackup.getarchive

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.common.extensions.getFileSharingIntent
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.snackbar.DefaultSnackbarVisuals
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants

@Composable
fun ExportGetArchiveRoute(
    navigateBackToSettingsDestination: () -> Unit,
    navigateToExportAuthDestination: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    viewModel: ExportGetArchiveViewModel = hiltViewModel(),
) {
    val exportGetArchiveState: ExportGetArchiveUiState by viewModel.exportGetArchiveState.collectAsStateWithLifecycle()

    val context: Context = LocalContext.current
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(ImportExportAndroidConstants.MimeTypeOs6lsb),
    ) { uri ->
        uri?.let {
            viewModel.saveFile(uri, context)
        } // null uri means that user has cancelled the process
    }

    when (val state = exportGetArchiveState) {
        is ExportGetArchiveUiState.Idle -> {
            // No-op
        }
        is ExportGetArchiveUiState.RestartExport -> {
            navigateToExportAuthDestination()
            state.reset()
        }
        is ExportGetArchiveUiState.Error -> state.dialogState.DefaultAlertDialog()
        is ExportGetArchiveUiState.Success -> {
            when (state.type) {
                ExportGetArchiveUiState.Type.Save -> {
                    showSnackBar(
                        DefaultSnackbarVisuals(
                            message = stringResource(id = OSString.export_backup_success_confirmation),
                            withDismissAction = true,
                            duration = SnackbarDuration.Short,
                            actionLabel = null,
                        ),
                    )
                }
                ExportGetArchiveUiState.Type.Share -> {
                    // No user feedback for now as we can't know if sharing intent has been canceled or not. Maybe one day 🤷‍
                }
            }
            state.reset()
        }
    }

    ExportGetArchiveScreen(
        shareFile = {
            viewModel.archiveFile?.let { archiveFile ->
                context.startActivity(
                    context.getFileSharingIntent(
                        fileToShare = archiveFile,
                        mimeType = AppConstants.FileProvider.MimeTypeZip,
                    ),
                )
            } ?: viewModel.emitError()
        },
        saveFile = {
            viewModel.archiveFile?.let { archiveFile ->
                saveFileLauncher.launch(archiveFile.name) // input represents file's pre-filled. Use is able to change it.
            } ?: viewModel.emitError()
        },
        navigateBackToSettingsDestination = {
            viewModel.archiveFile?.delete()
            navigateBackToSettingsDestination()
        },
    )
}
