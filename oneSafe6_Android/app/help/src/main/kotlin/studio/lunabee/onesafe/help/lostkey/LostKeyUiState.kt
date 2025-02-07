package studio.lunabee.onesafe.help.lostkey

import android.net.Uri

sealed interface LostKeyUiState {
    data object Idle : LostKeyUiState
    class ExitToMain(val backupUri: Uri) : LostKeyUiState
}
