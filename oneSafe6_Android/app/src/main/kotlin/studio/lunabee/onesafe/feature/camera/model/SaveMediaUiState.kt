package studio.lunabee.onesafe.feature.camera.model

import android.net.Uri
import studio.lunabee.onesafe.error.OSError

sealed interface SaveMediaUiState {
    data object Idle : SaveMediaUiState
    data object Loading : SaveMediaUiState
    data class Success(
        val thumbnailUri: Uri?,
        val captureUri: Uri?,
        val mediaType: OSMediaType,
    ) : SaveMediaUiState

    data class Error(val error: OSError) : SaveMediaUiState
}
