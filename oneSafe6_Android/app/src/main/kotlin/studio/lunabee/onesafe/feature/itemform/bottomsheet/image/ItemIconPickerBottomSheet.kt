package studio.lunabee.onesafe.feature.itemform.bottomsheet.image

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.composable.ItemIconPickerBottomSheetContent
import studio.lunabee.onesafe.common.composable.rememberOSCameraPicker
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemIconPickerBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onIconPickedByUser: (osImageSpec: OSImageSpec?) -> Unit,
    onImageCaptureFromCamera: (CameraData) -> Unit,
    cameraData: CameraData,
    removeImageSelected: () -> Unit,
    hasImageToDisplay: Boolean,
    canFetchFromUrl: Boolean,
    onFetchFromUrl: () -> Unit,
    onEnterUrlForIcon: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPicker = rememberOSCameraPicker(
        onCancel = onBottomSheetClosed,
        onImageCaptureFromCamera = onImageCaptureFromCamera,
        cameraData = cameraData,
        snackbarHostState = snackbarHostState,
        captureConfig = CaptureConfig.ItemIcon,
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onIconPickedByUser(uri?.let { OSImageSpec.Uri(uri = uri) }) },
    )

    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        snackbarHost = { sheetState ->
            SnackbarHostBottomSheet(snackbarHostState, sheetState, null)
        },
    ) { closeBottomSheet, paddingValues ->
        ItemIconPickerBottomSheetContent(
            paddingValues = paddingValues,
            takePicture = cameraPicker::onFileFromCameraRequested,
            pickImage = {
                closeBottomSheet()
                pickImageLauncher.launch(AppConstants.FileProvider.LauncherImageFilter)
            },
            modifier = Modifier
                .padding(bottom = OSDimens.SystemSpacing.Small)
                .testTag(tag = UiConstants.TestTag.BottomSheet.ItemImagePickerBottomSheet),
            deleteImage = {
                closeBottomSheet()
                removeImageSelected()
            }.takeIf { hasImageToDisplay },
            fetchFromUrl = {
                closeBottomSheet()
                onFetchFromUrl()
            }.takeIf { canFetchFromUrl },
            enterUrlForIcon = {
                closeBottomSheet()
                onEnterUrlForIcon()
            },
        )
    }
}
