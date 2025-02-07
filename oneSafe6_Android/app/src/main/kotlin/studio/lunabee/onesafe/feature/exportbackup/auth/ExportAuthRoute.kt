package studio.lunabee.onesafe.feature.exportbackup.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.importexport.ExportMetadata

// TODO check if ExportWorker is already running and handle UI gracefully

@Composable
fun ExportAuthRoute(
    navigateBack: () -> Unit,
    navigateToExportDataDestination: (itemCount: Int, contactCount: Int, safeNav: Boolean) -> Unit,
    viewModel: ExportAuthViewModel = hiltViewModel(),
) {
    val exportAuthState: ExportAuthUiState by viewModel.exportAuthState.collectAsStateWithLifecycle()
    val exportMetadata: ExportMetadata? by viewModel.exportMetadata.collectAsStateWithLifecycle()
    val safeExportMetadata: ExportMetadata? = exportMetadata

    fun startExport(safeNav: Boolean = true) {
        safeExportMetadata?.let {
            navigateToExportDataDestination(safeExportMetadata.itemCount, safeExportMetadata.contactCount, safeNav)
            (exportAuthState as? ExportAuthUiState.PasswordValid)?.reset?.invoke()
        }
    }

    if (safeExportMetadata == null) {
        // TODO need spec for error like this (i.e fail to get database content).
        Toast.makeText(LocalContext.current, stringResource(id = OSString.error_defaultMessage), Toast.LENGTH_LONG).show()
        navigateBack()
    } else {
        when (exportAuthState) {
            is ExportAuthUiState.WaitForPassword,
            is ExportAuthUiState.PasswordIncorrect,
            -> {
                // No-op: handled directly by UI
            }
            is ExportAuthUiState.PasswordValid -> {
                startExport()
            }
        }

        if (safeExportMetadata.isEmpty()) {
            ExportEmptyScreen(
                navigateBack = navigateBack,
            )
        } else {
            ExportAuthScreen(
                itemCount = safeExportMetadata.itemCount,
                contactCount = safeExportMetadata.contactCount,
                isCheckingPassword = exportAuthState is ExportAuthUiState.CheckingPassword,
                resetCredentialsError = (exportAuthState as? ExportAuthUiState.PasswordIncorrect)?.reset,
                checkPassword = viewModel::checkPassword,
                navigateBack = navigateBack,
            )
        }
    }
}
