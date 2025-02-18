package studio.lunabee.onesafe.feature.camera.model

data class CaptureUiState(
    val orientation: Orientation = Orientation.ROTATION_0,
    val isFlashEnabled: Boolean = false,
    val isSoundEnabled: Boolean = false,
    val mode: CameraMode = CameraMode.PHOTO,
    val isLoading: Boolean = false,
)

enum class CameraMode {
    VIDEO, PHOTO
}
