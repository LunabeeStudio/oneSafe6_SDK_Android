package studio.lunabee.onesafe.feature.migration.savedata

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataScreen
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.feature.migration.MigrationPermissionRationaleDialogState

@Composable
fun MigrationSaveDataRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    viewModel: MigrationSaveDataViewModel = hiltViewModel(),
    showSnackBar: (SnackbarVisuals) -> Unit,
) {
    val importSaveDataState: ImportSaveDataUiState by viewModel.importSaveDataState.collectAsStateWithLifecycle()
    val currentSafeItemCount by viewModel.currentSafeItemCount.collectAsStateWithLifecycle(0)

    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    var snackbarState: SnackbarState? by remember { mutableStateOf(null) }
    snackbarState?.let {
        showSnackBar(it.snackbarVisuals)
        snackbarState = null
    }

    LoadingBackHandler(enabled = importSaveDataState is ImportSaveDataUiState.ImportInProgress) {
        // Block back navigation.
    }

    when (val state = importSaveDataState) {
        is ImportSaveDataUiState.Success -> navigateToHome()
        is ImportSaveDataUiState.Dialog -> state.dialogState.DefaultAlertDialog()
        is ImportSaveDataUiState.ImportInProgress,
        is ImportSaveDataUiState.WaitingForUserChoice,
        is ImportSaveDataUiState.ExitWithError,
        -> {
            // Handle directly in screen
        }
    }

    val context = LocalContext.current

    val migrationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startMigration()
        } else {
            snackbarState = MessageSnackBarState(LbcTextSpec.StringResource(OSString.migration_permission_deniedFeedback))
        }
    }

    ImportSaveDataScreen(
        currentSafeItemCount = currentSafeItemCount,
        itemCount = viewModel.metadataResult.data?.itemCount,
        importSaveDataState = importSaveDataState,
        navigateBack = navigateBack,
        startImport = { mode ->
            viewModel.setSelectedMode(mode)
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    AppConstants.Migration.OldOneSafeServicePermission,
                ) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.startMigration()
                }
                shouldShowRequestPermissionRationale(
                    context.findFragmentActivity(),
                    AppConstants.Migration.OldOneSafeServicePermission,
                ) -> {
                    dialogState = MigrationPermissionRationaleDialogState(
                        launchPermissionRequest = {
                            migrationPermissionLauncher.launch(AppConstants.Migration.OldOneSafeServicePermission)
                        },
                        dismiss = { dialogState = null },
                    )
                }
                else -> {
                    migrationPermissionLauncher.launch(AppConstants.Migration.OldOneSafeServicePermission)
                }
            }
        },
    )
}
