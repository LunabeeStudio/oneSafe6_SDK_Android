package studio.lunabee.onesafe.feature.camera

import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.feature.camera.composable.CameraPreviewRoute
import studio.lunabee.onesafe.feature.camera.composable.ImagePreviewScreen
import studio.lunabee.onesafe.feature.camera.composable.VideoPreviewScreen
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.camera.model.SaveMediaUiState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun CameraActivityScreen(
    viewModel: CameraActivityViewModel,
    onSuccess: (thumbnailUri: Uri?, captureUri: Uri?, mediaType: OSMediaType) -> Unit,
    onCancel: () -> Unit,
    captureConfig: CaptureConfig,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraUiState by viewModel.cameraUiState.collectAsStateWithLifecycle()
    val videoCaptured by viewModel.videoCaptured.collectAsStateWithLifecycle()
    val imageCaptureUiState by viewModel.captureUiState.collectAsStateWithLifecycle()
    val cameraProviderUiState by viewModel.cameraProviderUiState.collectAsStateWithLifecycle()
    val isMaterialYouEnabled by viewModel.isMaterialYouEnabled.collectAsStateWithLifecycle(false)
    val snackbarHostState = remember { SnackbarHostState() }

    val snackbarState by viewModel.mainSnackbarData.collectAsStateWithLifecycle()
    val snackbarVisuals = snackbarState?.snackbarVisuals
    LaunchedEffect(snackbarState) {
        snackbarVisuals?.let { snackbarHostState.showSnackbar(it) }
    }

    val cameraController = remember { LifecycleCameraController(context) }
    val previewView: PreviewView = remember {
        PreviewView(context).apply {
            this.controller = cameraController
            cameraController.bindToLifecycle(lifecycleOwner)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = when (captureConfig) {
                CaptureConfig.ItemIcon -> PreviewView.ScaleType.FILL_CENTER
                CaptureConfig.FieldFile -> PreviewView.ScaleType.FIT_CENTER
            }
        }
    }

    // Adapt Camera Controller with updated settings
    LaunchedEffect(cameraProviderUiState.lensFacing, cameraProviderUiState.cameraOption, imageCaptureUiState.mode) {
        viewModel.setupCameraController(
            context = context,
            cameraController = cameraController,
            previewView = previewView,
        )
    }

    val saveMediaUiState = cameraUiState.saveMediaUiState
    LaunchedEffect(saveMediaUiState) {
        when (saveMediaUiState) {
            SaveMediaUiState.Idle -> {}
            SaveMediaUiState.Loading -> {}
            is SaveMediaUiState.Success -> onSuccess(saveMediaUiState.thumbnailUri, saveMediaUiState.captureUri, saveMediaUiState.mediaType)
            is SaveMediaUiState.Error -> {
                snackbarHostState.showSnackbar(
                    ErrorSnackbarState(error = saveMediaUiState.error, onClick = {}).getSnackbarVisuals(context),
                )
            }
        }
    }

    LaunchedEffect(imageCaptureUiState) {
        cameraController.imageCaptureFlashMode = if (imageCaptureUiState.isFlashEnabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    OSTheme(
        isMaterialYouSettingsEnabled = isMaterialYouEnabled,
        isSystemInDarkTheme = false,
    ) {
        Box(
            modifier = Modifier
                .testTag(UiConstants.TestTag.Screen.CameraActivityScreen)
                .fillMaxSize(),
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .zIndex(UiConstants.SnackBar.ZIndex),
            )
            val imageCaptured = cameraUiState.imageCaptured
            when {
                videoCaptured != null -> {
                    videoCaptured?.let {
                        VideoPreviewScreen(
                            onNavigateBack = viewModel::deleteActualMedia,
                            onConfirm = viewModel::saveVideo,
                            file = it,
                            isLoading = saveMediaUiState == SaveMediaUiState.Loading || saveMediaUiState is SaveMediaUiState.Success,
                        )
                    }
                }
                imageCaptured != null -> {
                    ImagePreviewScreen(
                        bitmap = imageCaptured,
                        onNavigateBack = viewModel::deleteActualMedia,
                        onConfirm = viewModel::saveImage,
                        isLoading = saveMediaUiState == SaveMediaUiState.Loading || saveMediaUiState is SaveMediaUiState.Success,
                        captureConfig = captureConfig,
                    )
                }
                else -> {
                    CameraPreviewRoute(
                        viewModel = viewModel,
                        previewView = previewView,
                        navigateBack = onCancel,
                        cameraController = cameraController,
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }
}
