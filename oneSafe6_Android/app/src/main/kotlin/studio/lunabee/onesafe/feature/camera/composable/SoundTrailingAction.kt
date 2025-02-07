package studio.lunabee.onesafe.feature.camera.composable

import android.Manifest
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.ui.res.OSDimens

@OptIn(ExperimentalPermissionsApi::class)
fun soundTrailingAction(
    isSoundEnabled: Boolean,
    toggle: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
): TopAppBarOptionTrailing = TopAppBarOptionTrailing {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val deniedFeedbackSnackbarVisual = ErrorSnackbarState(
        message = LbcTextSpec.StringResource(OSString.cameraScreen_audioPermissionError_message),
        onClick = {},
    ).snackbarVisuals

    val cameraPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { isGranted ->
        if (isGranted) {
            toggle()
        } else {
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context.findFragmentActivity(),
                Manifest.permission.RECORD_AUDIO,
            )
            if (!shouldShowRationale) {
                coroutineScope.launch { snackbarHostState.showSnackbar(deniedFeedbackSnackbarVisual) }
            }
        }
    }

    OSIconButton(
        image = if (isSoundEnabled) {
            OSImageSpec.Drawable(OSDrawable.ic_volume_up)
        } else {
            OSImageSpec.Drawable(OSDrawable.ic_volume_off)
        },
        onClick = {
            if (isSoundEnabled) {
                toggle()
            } else {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        colors = if (isSoundEnabled) {
            OSIconButtonDefaults.primaryIconButtonColors()
        } else {
            OSIconButtonDefaults.secondaryIconButtonColors()
        },
        modifier = modifier,
        buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
        contentDescription = if (isSoundEnabled) {
            LbcTextSpec.StringResource(OSString.accessibility_camera_disableSound)
        } else {
            LbcTextSpec.StringResource(OSString.accessibility_camera_enableSound)
        },
    )
}
