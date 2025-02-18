package studio.lunabee.onesafe.feature.share.file

import java.io.File

interface ShareFileUiState {

    object Idle : ShareFileUiState

    data class Data(
        val password: String,
        val file: File?,
        val itemsNbr: Int,
    ) : ShareFileUiState
}
