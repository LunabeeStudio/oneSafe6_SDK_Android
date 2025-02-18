package studio.lunabee.onesafe.feature.importbackup.selectfile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.snackbar.DefaultSnackbarVisuals
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants

@Composable
fun ImportFileRoute(
    navigateBack: () -> Unit,
    navigateToImportAuthDestination: () -> Unit,
    navigateToWarningNotFullySupportedArchive: () -> Unit,
    showSnackBar: ((SnackbarVisuals) -> Unit)? = null,
    viewModel: ImportFileViewModel = hiltViewModel(),
) {
    val context: Context = LocalContext.current
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { // uri can be null if user cancel file selection (not an error)
            viewModel.unzipFileAndGetMetadata(uri = uri, context = context)
        }
    }

    val metadataReadState: MetadataReadState by viewModel.importMetadataState.collectAsStateWithLifecycle()

    LoadingBackHandler(enabled = metadataReadState !is MetadataReadState.ExtractInProgress, onBack = navigateBack)

    when (val state = metadataReadState) {
        is MetadataReadState.Success -> {
            navigateToImportAuthDestination()
            viewModel.setMetadataReadStateToInitialValue()
        }
        is MetadataReadState.NotFullySupported -> {
            navigateToWarningNotFullySupportedArchive()
            viewModel.setMetadataReadStateToInitialValue()
        }
        is MetadataReadState.Error -> state.dialogState.DefaultAlertDialog()
        is MetadataReadState.LaunchPicker -> {
            launchFilePicker(pickFileLauncher)
            state.resetState()
        }
        is MetadataReadState.StartExtract -> state.extract(context)
        is MetadataReadState.WaitingForExtract -> {
            // No-op
        }

        is MetadataReadState.ExitWithError -> {
            LaunchedEffect(state) {
                showSnackBar?.invoke(
                    DefaultSnackbarVisuals(
                        message = state.error.string(context),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                        actionLabel = null,
                    ),
                )
                navigateBack()
            }
        }
    }
    ImportFileScreen(
        navigateBack = navigateBack,
        pickFile = { launchFilePicker(pickFileLauncher) },
        extractProgress = (metadataReadState as? MetadataReadState.ExtractInProgress)?.progress,
    )
}

private fun launchFilePicker(pickFileLauncher: ManagedActivityResultLauncher<String, Uri?>) {
    pickFileLauncher.launch(ImportExportAndroidConstants.MimeTypeOs6lsb)
}
