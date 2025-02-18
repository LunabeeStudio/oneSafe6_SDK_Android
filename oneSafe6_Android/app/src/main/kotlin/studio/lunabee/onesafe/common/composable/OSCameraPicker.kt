package studio.lunabee.onesafe.common.composable

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.feature.camera.CameraActivity
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.itemform.bottomsheet.image.CameraPickerPermissionRationaleDialogState

interface OSCameraPicker {
    fun onFileFromCameraRequested()
}

@OptIn(ExperimentalPermissionsApi::class)
private class OSDefaultCameraPicker(
    private val cameraPermissionState: PermissionState,
    private val setPermissionDialogState: (DialogState?) -> Unit,
    private val startCapture: () -> Unit,
) : OSCameraPicker {
    override fun onFileFromCameraRequested() {
        when (cameraPermissionState.status) {
            PermissionStatus.Granted -> {
                startCapture()
            }
            is PermissionStatus.Denied -> {
                if (cameraPermissionState.status.shouldShowRationale) {
                    setPermissionDialogState(
                        CameraPickerPermissionRationaleDialogState(
                            launchPermissionRequest = {
                                cameraPermissionState.launchPermissionRequest()
                                setPermissionDialogState(null)
                            },
                            dismiss = { setPermissionDialogState(null) },
                        ),
                    )
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberOSCameraPicker(
    onCancel: () -> Unit,
    onImageCaptureFromCamera: (CameraData) -> Unit,
    cameraData: CameraData,
    snackbarHostState: SnackbarHostState,
    captureConfig: CaptureConfig,
): OSCameraPicker {
    val context: Context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var extCameraData: CameraData? by remember {
        mutableStateOf(null)
    }

    val takePictureExternalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { result ->
            val data = extCameraData
            if (result && data != null) {
                onImageCaptureFromCamera(data)
            } else {
                onCancel()
            }
            extCameraData = null
        },
    )

    val takePictureInAppLauncher = rememberLauncherForActivityResult(
        contract = CameraActivity.Contract(),
        onResult = { (resultCode, inAppPhotoCapture) ->
            if (resultCode == Activity.RESULT_OK && inAppPhotoCapture != null) {
                onImageCaptureFromCamera(CameraData.InApp(inAppPhotoCapture))
            } else {
                onCancel()
            }
        },
    )

    fun startCapture() {
        when (cameraData) {
            is CameraData.InApp -> takePictureInAppLauncher.launch(CameraActivity.Input(captureConfig, cameraData.photoCapture))
            is CameraData.External -> {
                takePictureExternalLauncher.launch(cameraData.photoCapture.value.publicUri)
                extCameraData = cameraData
            }
        }
    }

    var permissionDialogState by rememberDialogState()
    permissionDialogState?.DefaultAlertDialog()
    val deniedFeedbackSnackbarVisual =
        ErrorSnackbarState(
            message = LbcTextSpec.StringResource(OSString.imagePicker_permission_rationale_deniedFeedback),
            onClick = {},
        ).snackbarVisuals

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) { isGranted ->
        if (isGranted) {
            startCapture()
        } else {
            // Manually get shouldShowRationale https://github.com/google/accompanist/issues/1690
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context.findFragmentActivity(),
                Manifest.permission.CAMERA,
            )
            if (!shouldShowRationale) {
                coroutineScope.launch { snackbarHostState.showSnackbar(deniedFeedbackSnackbarVisual) }
            }
        }
    }

    return remember(cameraData.cameraSystem) {
        OSDefaultCameraPicker(
            cameraPermissionState = cameraPermissionState,
            setPermissionDialogState = { permissionDialogState = it },
            startCapture = ::startCapture,
        )
    }
}
