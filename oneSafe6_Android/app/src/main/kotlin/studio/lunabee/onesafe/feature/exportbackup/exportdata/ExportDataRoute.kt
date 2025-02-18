package studio.lunabee.onesafe.feature.exportbackup.exportdata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbloading.LoadingBackHandler
import java.io.File

@Composable
fun ExportDataRoute(
    navigateBack: () -> Unit,
    navigateToExportGetArchiveDestination: (extractedArchivePath: String) -> Unit,
    itemCount: Int,
    contactCount: Int,
    viewModel: ExportDataViewModel = hiltViewModel(),
) {
    val exportDataState: LBFlowResult<File> by viewModel.exportDataState.collectAsStateWithLifecycle()
    LoadingBackHandler(enabled = exportDataState is LBFlowResult.Loading) {
        // No-op: lock back during export. Maybe we should add a SnackBar feedback (V2).
    }
    when (val state = exportDataState) {
        is LBFlowResult.Failure,
        is LBFlowResult.Loading,
        -> {
            // No-op: handle directly by UI.
        }
        is LBFlowResult.Success -> {
            navigateToExportGetArchiveDestination(state.successData.absolutePath)
        }
    }

    ExportDataScreen(
        itemCount = itemCount,
        contactCount = contactCount,
        navigateBack = navigateBack,
        isProcessingExport = exportDataState is LBFlowResult.Loading,
        isExportInError = exportDataState is LBFlowResult.Failure,
    )
}
