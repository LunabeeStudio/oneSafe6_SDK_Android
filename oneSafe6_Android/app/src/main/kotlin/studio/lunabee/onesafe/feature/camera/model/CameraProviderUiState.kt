package studio.lunabee.onesafe.feature.camera.model

import androidx.camera.core.CameraSelector

data class CameraProviderUiState(
    val lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val cameraOption: CameraOption = CameraOption.NormalMode,
    val availableOptions: List<CameraOption> = emptyList(),
)
