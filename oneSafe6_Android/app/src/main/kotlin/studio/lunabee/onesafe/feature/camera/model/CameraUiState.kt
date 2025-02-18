package studio.lunabee.onesafe.feature.camera.model

import android.graphics.Bitmap

data class CameraUiState(
    val isCryptoLoaded: Boolean,
    val showTips: Boolean,
    val imageCaptured: Bitmap?,
    val saveMediaUiState: SaveMediaUiState,
) {
    companion object {
        fun default(): CameraUiState = CameraUiState(
            isCryptoLoaded = true,
            showTips = false,
            imageCaptured = null,
            saveMediaUiState = SaveMediaUiState.Idle,
        )
    }
}
