package studio.lunabee.onesafe.feature.exportbackup.auth

interface ExportAuthUiState {
    object WaitForPassword : ExportAuthUiState
    data class PasswordValid(val reset: () -> Unit) : ExportAuthUiState
    data class PasswordIncorrect(val reset: () -> Unit) : ExportAuthUiState
    object CheckingPassword : ExportAuthUiState
}
