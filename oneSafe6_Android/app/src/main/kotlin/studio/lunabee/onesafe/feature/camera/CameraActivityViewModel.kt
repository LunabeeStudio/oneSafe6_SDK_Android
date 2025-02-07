package studio.lunabee.onesafe.feature.camera

import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.camera.delegate.CameraStateDelegate
import studio.lunabee.onesafe.feature.camera.delegate.CameraStateDelegateImpl
import studio.lunabee.onesafe.feature.camera.delegate.VideoDelegate
import studio.lunabee.onesafe.feature.camera.delegate.VideoDelegateImpl
import studio.lunabee.onesafe.feature.camera.model.CameraUiState
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.camera.model.SaveMediaUiState
import studio.lunabee.onesafe.feature.camera.utils.toOrientedBitmap
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.usecase.SaveImageFromCameraForItemEditingUseCase
import studio.lunabee.onesafe.usecase.SaveVideoFromCameraForItemEditingUseCase
import java.io.File

@AssistedFactory
interface CameraViewModelFactory {
    fun create(
        captureConfig: CaptureConfig,
        @Assisted("thumbnail") thumbnailFile: File?,
        @Assisted("capture") captureFile: File?,
    ): CameraActivityViewModel
}

@HiltViewModel(assistedFactory = CameraViewModelFactory::class)
class CameraActivityViewModel @AssistedInject constructor(
    private val saveImageFromCameraForItemEditingUseCase: SaveImageFromCameraForItemEditingUseCase,
    private val saveVideoFromCameraForItemEditingUseCase: SaveVideoFromCameraForItemEditingUseCase,
    getAppVisitUseCase: GetAppVisitUseCase,
    private val setAppVisitUseCase: SetAppVisitUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
    @Assisted val captureConfig: CaptureConfig,
    @Assisted("thumbnail") val thumbnailFile: File?,
    @Assisted("capture") val captureFile: File?,
    private val cameraStateDelegate: CameraStateDelegateImpl,
    private val videoDelegate: VideoDelegateImpl,
    isSafeReadyUseCase: IsSafeReadyUseCase,
) : ViewModel(),
    CameraStateDelegate by cameraStateDelegate,
    VideoDelegate by videoDelegate {
    val isMaterialYouEnabled: Flow<Boolean> = getAppSettingUseCase.materialYou()

    val mainSnackbarData: StateFlow<SnackbarState?> = merge(
        videoSnackbarData,
        cameraStateSnackbarData,
    ).stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _imageCaptured: MutableStateFlow<Bitmap?> = MutableStateFlow(null)
    private val _saveUiState: MutableStateFlow<SaveMediaUiState> = MutableStateFlow(SaveMediaUiState.Idle)
    private val _cameraUiState: MutableStateFlow<CameraUiState> = MutableStateFlow(CameraUiState.default())
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    init {
        combine(
            isSafeReadyUseCase.flow(),
            getAppVisitUseCase.hasHiddenCameraTips(),
            _imageCaptured,
            _saveUiState,
        ) { isCryptoLoaded, hasHiddenTips, imageCaptured, saveUiState ->
            val state = CameraUiState(
                isCryptoLoaded = isCryptoLoaded,
                showTips = !hasHiddenTips,
                imageCaptured = imageCaptured,
                saveMediaUiState = saveUiState,
            )
            _cameraUiState.value = state
        }.launchIn(viewModelScope)
    }

    val imageCaptureCallback: ImageCapture.OnImageCapturedCallback = object : ImageCapture.OnImageCapturedCallback() {
        override fun onError(exc: ImageCaptureException) {
            _saveUiState.value = SaveMediaUiState.Error(
                error = OSAppError(code = OSAppError.Code.IMAGE_CAPTURE_FAILED, cause = exc),
            )
            hideLoading()
        }

        @ExperimentalGetImage
        override fun onCaptureSuccess(image: ImageProxy) {
            viewModelScope.launch(Dispatchers.IO) {
                _imageCaptured.value = image.toOrientedBitmap()
                hideLoading()
            }
        }
    }

    fun deleteActualMedia() {
        _imageCaptured.value?.recycle()
        _imageCaptured.value = null
        deleteVideoCaptured()
    }

    fun saveImage() {
        val imageToSave = _imageCaptured.value ?: return
        viewModelScope.launch {
            _saveUiState.value = SaveMediaUiState.Loading
            saveImageFromCameraForItemEditingUseCase(imageToSave, InAppMediaCapture(thumbnailFile, captureFile, OSMediaType.PHOTO))
            _saveUiState.value = SaveMediaUiState.Success(thumbnailFile?.toUri(), captureFile?.toUri(), OSMediaType.PHOTO)
        }
    }

    fun saveVideo() {
        val videoToSave = videoCaptured.value ?: return
        viewModelScope.launch {
            _saveUiState.value = SaveMediaUiState.Loading
            saveVideoFromCameraForItemEditingUseCase(
                videoToSave,
                InAppMediaCapture(thumbnailFile, captureFile, OSMediaType.VIDEO),
            )
            _saveUiState.value = SaveMediaUiState.Success(thumbnailFile?.toUri(), captureFile?.toUri(), OSMediaType.VIDEO)
        }
    }

    fun onHideTips() {
        viewModelScope.launch {
            setAppVisitUseCase.setHasHiddenCameraTips()
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoDelegate.close()
    }
}
