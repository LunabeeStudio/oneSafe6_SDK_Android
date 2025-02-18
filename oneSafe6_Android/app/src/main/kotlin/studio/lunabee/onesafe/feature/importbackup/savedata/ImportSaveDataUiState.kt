package studio.lunabee.onesafe.feature.importbackup.savedata

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState

@Stable
sealed interface ImportSaveDataUiState {
    data object WaitingForUserChoice : ImportSaveDataUiState
    data class ImportInProgress(val progress: Float) : ImportSaveDataUiState
    data class Dialog(val dialogState: DialogState) : ImportSaveDataUiState
    data class ExitWithError(val error: ErrorSnackbarState?) : ImportSaveDataUiState
    data object Success : ImportSaveDataUiState
}

val ImportSaveDataUiState.isAllowingAction: Boolean
    get() = !isProcessing && !isFinished

val ImportSaveDataUiState.isFinished: Boolean
    get() = this is ImportSaveDataUiState.Success

val ImportSaveDataUiState.isProcessing: Boolean
    get() = this is ImportSaveDataUiState.ImportInProgress
