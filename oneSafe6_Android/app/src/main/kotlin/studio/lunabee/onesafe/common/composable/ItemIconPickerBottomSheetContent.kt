package studio.lunabee.onesafe.common.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ItemIconPickerBottomSheetContent(
    paddingValues: PaddingValues,
    takePicture: () -> Unit,
    pickImage: () -> Unit,
    deleteImage: (() -> Unit)?,
    fetchFromUrl: (() -> Unit)?,
    enterUrlForIcon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = modifier,
    ) {
        OSClickableRow(
            text = LbcTextSpec.StringResource(OSString.itemCreation_picture_choice_fromCamera),
            onClick = takePicture,
            leadingIcon = {
                OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_from_camera))
            },
            contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                index = 0,
                elementsCount = Int.MAX_VALUE,
            ),
        )
        OSClickableRow(
            text = LbcTextSpec.StringResource(OSString.itemCreation_picture_choice_fromGallery),
            onClick = pickImage,
            leadingIcon = {
                OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_from_library))
            },
        )
        OSClickableRow(
            text = LbcTextSpec.StringResource(OSString.safeItemDetail_icon_import_fromUrl),
            onClick = enterUrlForIcon,
            leadingIcon = {
                OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_image_search))
            },
        )

        fetchFromUrl?.let {
            OSClickableRow(
                text = LbcTextSpec.StringResource(OSString.itemCreation_picture_fromUrl),
                onClick = fetchFromUrl,
                leadingIcon = {
                    OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_magic))
                },
            )
        }

        deleteImage?.let {
            OSClickableRow(
                text = LbcTextSpec.StringResource(OSString.itemCreation_picture_choice_delete),
                onClick = deleteImage,
                leadingIcon = {
                    OSIconAlertDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_delete))
                },
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun ItemImagePickerBottomSheetContentPreview() {
    OSTheme {
        ItemIconPickerBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            takePicture = { },
            pickImage = { },
            deleteImage = { },
            fetchFromUrl = { },
            enterUrlForIcon = { },
        )
    }
}

@OsDefaultPreview
@Composable
private fun ImagePickerWithDeleteBottomSheetContentPreview() {
    OSTheme {
        ItemIconPickerBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            takePicture = { },
            pickImage = { },
            deleteImage = { },
            fetchFromUrl = { },
            enterUrlForIcon = { },
        )
    }
}
