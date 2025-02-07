package studio.lunabee.onesafe.feature.camera.composable

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.camera.CameraActivity
import studio.lunabee.onesafe.feature.camera.CameraActivityViewModel
import studio.lunabee.onesafe.feature.camera.model.CameraMode
import studio.lunabee.onesafe.feature.camera.model.CameraOption
import studio.lunabee.onesafe.feature.camera.model.CameraProviderUiState
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.camera.model.CaptureUiState
import studio.lunabee.onesafe.feature.camera.model.RecordTimerInfo
import studio.lunabee.onesafe.feature.camera.model.RecordingState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun CameraPreviewRoute(
    cameraController: LifecycleCameraController,
    viewModel: CameraActivityViewModel,
    previewView: PreviewView,
    navigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val cameraProviderUiState by viewModel.cameraProviderUiState.collectAsStateWithLifecycle()
    val captureUiState by viewModel.captureUiState.collectAsStateWithLifecycle()
    val showTips = viewModel.cameraUiState.collectAsStateWithLifecycle().value.showTips
    val recordingState: RecordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val timerInfo by viewModel.timerInfo.collectAsStateWithLifecycle()

    CameraPreviewScreen(
        previewView = previewView,
        navigateBack = navigateBack,
        cameraProviderUiState = cameraProviderUiState,
        captureUiState = captureUiState,
        flipCamera = viewModel::flipCamera,
        toggleFlash = viewModel::toggleFlash,
        toggleSound = viewModel::toggleSound,
        selectOption = viewModel::selectOption,
        captureConfig = viewModel.captureConfig,
        recordingState = recordingState,
        onSelectMode = viewModel::selectCameraMode,
        takeVideo = { viewModel.recordVideo(cameraController, context, captureUiState.isSoundEnabled) },
        takePicture = {
            viewModel.displayLoading()
            cameraController.takePicture(
                ContextCompat.getMainExecutor(context),
                viewModel.imageCaptureCallback,
            )
        },
        timerInfo = timerInfo,
        isTipsDisplayed = showTips,
        onHideTips = viewModel::onHideTips,
        snackbarHostState = snackbarHostState,
        pauseRecording = viewModel::pauseRecording,
        resumeRecording = viewModel::resumeRecording,
    )
}

@Composable
private fun CameraPreviewScreen(
    previewView: PreviewView,
    navigateBack: () -> Unit,
    takePicture: () -> Unit,
    cameraProviderUiState: CameraProviderUiState,
    captureUiState: CaptureUiState,
    flipCamera: () -> Unit,
    toggleFlash: () -> Unit,
    toggleSound: () -> Unit,
    selectOption: (option: CameraOption) -> Unit,
    captureConfig: CaptureConfig,
    takeVideo: () -> Unit,
    onSelectMode: (CameraMode) -> Unit,
    timerInfo: RecordTimerInfo?,
    isTipsDisplayed: Boolean,
    onHideTips: () -> Unit,
    snackbarHostState: SnackbarHostState,
    recordingState: RecordingState,
    pauseRecording: () -> Unit,
    resumeRecording: () -> Unit,
) {
    var isMenuExtended by remember { mutableStateOf(false) }
    val animatedRotation by animateFloatAsState(
        targetValue = captureUiState.orientation.degrees,
        label = "AnimatedRotation",
        animationSpec = tween(),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            timerInfo?.let {
                TimerCell(timerInfo = timerInfo, recordingState = recordingState)
            }
            OSTopAppBar(
                modifier = Modifier,
                options = buildList {
                    if (recordingState == RecordingState.Idle) {
                        add(
                            TopAppBarOptionNav(
                                image = OSImageSpec.Drawable(OSDrawable.ic_close),
                                contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                                onClick = navigateBack,
                                state = OSActionState.Enabled,
                            ),
                        )
                    }
                    when {
                        captureUiState.mode == CameraMode.PHOTO -> add(
                            flashTrailingAction(
                                isFlashEnabled = captureUiState.isFlashEnabled,
                                modifier = Modifier.rotate(animatedRotation),
                                onClick = toggleFlash,
                            ),
                        )
                        captureUiState.mode == CameraMode.VIDEO && recordingState == RecordingState.Idle -> add(
                            soundTrailingAction(
                                isSoundEnabled = captureUiState.isSoundEnabled,
                                modifier = Modifier.rotate(animatedRotation),
                                toggle = toggleSound,
                                snackbarHostState = snackbarHostState,
                            ),
                        )
                        else -> Unit
                    }
                },
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            AndroidView(
                modifier = captureConfig.modifier
                    // Add some arbitrary bias to have smaller preview upper in screen
                    .align(BiasAlignment(0f, CameraActivity.VerticalImageBias)),
                factory = { previewView },
            )
            if (isTipsDisplayed) {
                CameraInfoCard(
                    onDismiss = onHideTips,
                    modifier = Modifier
                        .padding(OSDimens.SystemSpacing.Regular)
                        .align(Alignment.BottomCenter),
                )
            }
        }
        Column {
            OSSmallSpacer()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = OSDimens.SystemSpacing.Regular),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                when (recordingState) {
                    RecordingState.Idle -> CameraOptionButton(
                        modifier = Modifier.rotate(animatedRotation),
                        icon = OSDrawable.ic_union,
                        onClick = flipCamera,
                        contentDescription = LbcTextSpec.StringResource(OSString.accessibility_camera_switchCamera),
                    )
                    RecordingState.Paused -> CameraOptionButton(
                        modifier = Modifier.rotate(animatedRotation),
                        icon = OSDrawable.ic_play,
                        onClick = resumeRecording,
                        contentDescription = LbcTextSpec.StringResource(OSString.common_play),
                    )
                    RecordingState.Recording -> CameraOptionButton(
                        modifier = Modifier.rotate(animatedRotation),
                        icon = OSDrawable.ic_pause,
                        onClick = pauseRecording,
                        contentDescription = LbcTextSpec.StringResource(OSString.common_pause),
                    )
                }
                // Display shutter button or record button depending on Camera Mode
                when (captureUiState.mode) {
                    CameraMode.VIDEO -> RecordButton(
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Huge),
                        onClick = takeVideo,
                        isRecording = recordingState != RecordingState.Idle,
                    )
                    CameraMode.PHOTO -> ShutterButton(
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Huge),
                        onClick = takePicture,
                        isLoading = captureUiState.isLoading,
                    )
                }

                // We hide the options if we are in video mode because not supported, also hidden if no option available
                if (cameraProviderUiState.availableOptions.size > 1 && captureUiState.mode == CameraMode.PHOTO) {
                    Box {
                        CameraOptionButton(
                            icon = cameraProviderUiState.cameraOption.drawableRes,
                            onClick = { isMenuExtended = true },
                            modifier = Modifier.rotate(animatedRotation),
                            contentDescription = LbcTextSpec.StringResource(OSString.accessibility_camera_options),
                        )
                        CameraOptionMenu(
                            isMenuExpended = isMenuExtended,
                            onDismiss = { isMenuExtended = false },
                            options = cameraProviderUiState.availableOptions,
                            selectedOption = cameraProviderUiState.cameraOption,
                            onSelectedOption = selectOption,
                        )
                    }
                } else {
                    Box(Modifier.size(OSDimens.Camera.optionButtonSize))
                }
            }
            if (captureConfig == CaptureConfig.FieldFile) {
                CameraModeSelector(
                    selectedMode = captureUiState.mode,
                    onSelectMode = if (recordingState == RecordingState.Idle) onSelectMode else { _ -> },
                    modifier = Modifier
                        .padding(bottom = OSDimens.SystemSpacing.Regular)
                        .navigationBarsPadding()
                        .then(
                            if (recordingState != RecordingState.Idle) {
                                Modifier.alpha(0f)
                            } else {
                                Modifier
                            },
                        ),
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .size(OSDimens.SystemSpacing.Regular)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun CameraPreviewScreenFieldPreview() {
    Surface(color = Color.Black) {
        CameraPreviewScreen(
            takeVideo = {},
            previewView = PreviewView(LocalContext.current),
            navigateBack = {},
            takePicture = {},
            cameraProviderUiState = CameraProviderUiState(),
            captureUiState = CaptureUiState(),
            flipCamera = {},
            toggleFlash = {},
            selectOption = {},
            captureConfig = CaptureConfig.FieldFile,
            isTipsDisplayed = true,
            onHideTips = {},
            onSelectMode = {},
            snackbarHostState = remember { SnackbarHostState() },
            timerInfo = null,
            toggleSound = {},
            recordingState = RecordingState.Idle,
            pauseRecording = {},
            resumeRecording = {},
        )
    }
}

@OsDefaultPreview
@Composable
private fun CameraPreviewScreenItemPreview() {
    Surface(color = Color.Black) {
        CameraPreviewScreen(
            takeVideo = {},
            previewView = PreviewView(LocalContext.current),
            navigateBack = {},
            takePicture = {},
            cameraProviderUiState = CameraProviderUiState(),
            captureUiState = CaptureUiState(),
            flipCamera = {},
            toggleFlash = {},
            selectOption = {},
            captureConfig = CaptureConfig.ItemIcon,
            isTipsDisplayed = true,
            onHideTips = {},
            onSelectMode = {},
            snackbarHostState = remember { SnackbarHostState() },
            timerInfo = null,
            toggleSound = {},
            recordingState = RecordingState.Idle,
            pauseRecording = {},
            resumeRecording = {},
        )
    }
}
