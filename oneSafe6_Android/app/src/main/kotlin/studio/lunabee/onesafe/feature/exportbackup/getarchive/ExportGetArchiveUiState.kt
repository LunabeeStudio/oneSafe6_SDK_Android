package studio.lunabee.onesafe.feature.exportbackup.getarchive

import studio.lunabee.onesafe.commonui.dialog.DialogState

interface ExportGetArchiveUiState {
    object Idle : ExportGetArchiveUiState
    data class Error(val dialogState: DialogState) : ExportGetArchiveUiState

    /**
     * @param type is used to display the correct feedback.
     */
    data class Success(val type: Type, val reset: () -> Unit) : ExportGetArchiveUiState

    /**
     * In case of blocking error (i.e archive exported file not found), invite user to restart the process.
     */
    data class RestartExport(val reset: () -> Unit) : ExportGetArchiveUiState

    enum class Type {
        Save,
        Share,
    }
}
