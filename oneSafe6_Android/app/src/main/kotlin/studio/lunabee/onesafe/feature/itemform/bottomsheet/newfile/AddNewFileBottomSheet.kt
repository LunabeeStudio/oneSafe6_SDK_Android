@file:OptIn(ExperimentalMaterial3Api::class)

package studio.lunabee.onesafe.feature.itemform.bottomsheet.newfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.composable.rememberOSCameraPicker
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewFileBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onFileSelected: (List<Uri>) -> Unit,
    cameraData: CameraData,
    onImageCaptureFromCamera: (CameraData) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPicker = rememberOSCameraPicker(
        onCancel = onBottomSheetClosed,
        onImageCaptureFromCamera = onImageCaptureFromCamera,
        cameraData = cameraData,
        snackbarHostState = snackbarHostState,
        captureConfig = CaptureConfig.FieldFile,
    )

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uriList ->
            onFileSelected(uriList)
        },
    )

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uriList ->
            onFileSelected(uriList)
        },
    )

    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = {
            onBottomSheetClosed()
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        skipPartiallyExpanded = true,
        snackbarHost = { sheetState ->
            SnackbarHostBottomSheet(snackbarHostState, sheetState, null)
        },
    ) { closeBottomSheet, paddingValues ->
        AddNewFileBottomSheetContent(
            paddingValues = paddingValues,
            onFileFromCameraRequested = cameraPicker::onFileFromCameraRequested,
            onFileFromExplorerRequested = {
                pickFileLauncher.launch(AppConstants.FileProvider.LauncherAllFileFilter)
                closeBottomSheet()
            },
            onFileFromGalleryRequested = {
                val request = PickVisualMediaRequest.Builder().build()
                pickMediaLauncher.launch(request)
                closeBottomSheet()
            },
        )
    }
}
