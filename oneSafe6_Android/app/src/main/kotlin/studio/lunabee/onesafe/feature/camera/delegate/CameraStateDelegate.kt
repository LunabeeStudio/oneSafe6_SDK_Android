package studio.lunabee.onesafe.feature.camera.delegate

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import co.touchlab.kermit.Logger
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.camera.model.CameraMode
import studio.lunabee.onesafe.feature.camera.model.CameraOption
import studio.lunabee.onesafe.feature.camera.model.CameraProviderUiState
import studio.lunabee.onesafe.feature.camera.model.CaptureUiState
import studio.lunabee.onesafe.feature.camera.model.Orientation
import java.util.concurrent.ExecutionException
import javax.inject.Inject

private val logger: Logger = LBLogger.get("CameraStateDelegate")

interface CameraStateDelegate {
    val captureUiState: StateFlow<CaptureUiState>
    val cameraProviderUiState: StateFlow<CameraProviderUiState>
    val cameraStateSnackbarData: StateFlow<SnackbarState?>
    fun updateOrientation(orientation: Orientation)
    fun toggleFlash()
    fun toggleSound()
    fun displayLoading()
    fun hideLoading()
    fun flipCamera()
    fun selectCameraMode(cameraMode: CameraMode)
    fun selectOption(option: CameraOption)
    fun setupCameraController(
        context: Context,
        cameraController: LifecycleCameraController,
        previewView: PreviewView,
    )
}

class CameraStateDelegateImpl @Inject constructor() : CameraStateDelegate {
    private val _captureUiState: MutableStateFlow<CaptureUiState> = MutableStateFlow(CaptureUiState())
    override val captureUiState: StateFlow<CaptureUiState> = _captureUiState.asStateFlow()

    private val _cameraProviderUiState: MutableStateFlow<CameraProviderUiState> = MutableStateFlow(CameraProviderUiState())
    override val cameraProviderUiState: StateFlow<CameraProviderUiState> = _cameraProviderUiState.asStateFlow()

    private val _cameraStateSnackbarData: MutableStateFlow<SnackbarState?> = MutableStateFlow(null)
    override val cameraStateSnackbarData: StateFlow<SnackbarState?> = _cameraStateSnackbarData.asStateFlow()

    override fun updateOrientation(orientation: Orientation) {
        _captureUiState.value = captureUiState.value.copy(orientation = orientation)
    }

    override fun toggleFlash() {
        _captureUiState.value = captureUiState.value.copy(isFlashEnabled = !captureUiState.value.isFlashEnabled)
    }

    override fun toggleSound() {
        _captureUiState.value = captureUiState.value.copy(isSoundEnabled = !captureUiState.value.isSoundEnabled)
    }

    override fun displayLoading() {
        _captureUiState.value = captureUiState.value.copy(isLoading = true)
    }

    override fun hideLoading() {
        _captureUiState.value = captureUiState.value.copy(isLoading = false)
    }

    override fun flipCamera() {
        _cameraProviderUiState.value = cameraProviderUiState.value.copy(
            lensFacing = when (cameraProviderUiState.value.lensFacing) {
                CameraSelector.DEFAULT_BACK_CAMERA -> CameraSelector.DEFAULT_FRONT_CAMERA
                CameraSelector.DEFAULT_FRONT_CAMERA -> CameraSelector.DEFAULT_BACK_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            },
        )
    }

    override fun selectCameraMode(cameraMode: CameraMode) {
        if (cameraMode == CameraMode.VIDEO) {
            _cameraProviderUiState.value = _cameraProviderUiState.value.copy(cameraOption = CameraOption.NormalMode)
        }
        _captureUiState.value = captureUiState.value.copy(mode = cameraMode)
    }

    private fun updateAvailableOption(extensionsManager: ExtensionsManager) {
        val availableOptions = CameraOption.entries.filter {
            extensionsManager.isExtensionAvailable(cameraProviderUiState.value.lensFacing, it.extension)
        }
        _cameraProviderUiState.value = cameraProviderUiState.value.copy(availableOptions = availableOptions)
    }

    override fun selectOption(option: CameraOption) {
        _cameraProviderUiState.value = cameraProviderUiState.value.copy(cameraOption = option)
    }

    override fun setupCameraController(
        context: Context,
        cameraController: LifecycleCameraController,
        previewView: PreviewView,
    ) {
        val processCameraProvider = try {
            ProcessCameraProvider.getInstance(context).get()
        } catch (e: ExecutionException) {
            logger.e(e)
            _cameraStateSnackbarData.value = ErrorSnackbarState(OSAppError(OSAppError.Code.CAMERA_SETUP_FAILED, cause = e)) {}
            null
        } catch (e: InterruptedException) {
            logger.e(e)
            _cameraStateSnackbarData.value = ErrorSnackbarState(OSAppError(OSAppError.Code.CAMERA_SETUP_FAILED, cause = e)) {}
            null
        }
        processCameraProvider?.let {
            val extensionsManager = ExtensionsManager.getInstanceAsync(context, processCameraProvider).get()
            updateAvailableOption(extensionsManager)
            val preview = Preview
                .Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY).build(),
                )
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            processCameraProvider.unbindAll()
            cameraController.cameraSelector = extensionsManager.getExtensionEnabledCameraSelector(
                cameraProviderUiState.value.lensFacing,
                cameraProviderUiState.value.cameraOption.extension,
            )
            when (captureUiState.value.mode) {
                CameraMode.PHOTO -> cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                CameraMode.VIDEO -> cameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)
            }
        }
    }
}
