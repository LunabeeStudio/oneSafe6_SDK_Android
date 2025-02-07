package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.common.utils.drawableId
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.molecule.OSPlaceHolder
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.Alpha.EmojiBackground
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ItemImageChoice(
    currentImage: OSImageSpec?,
    openItemImagePickerBottomSheet: () -> Unit,
    loadingProgress: Float?,
    placeHolder: LbcTextSpec?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        ItemActionLoading(
            loadingProgress = loadingProgress,
        ) {
            if (currentImage != null) {
                OSImage(
                    image = currentImage,
                    modifier = Modifier
                        .size(size = OSDimens.SystemRoundContainerDimension.XLarge.dp)
                        .clip(shape = CircleShape)
                        .clickable(
                            onClick = openItemImagePickerBottomSheet,
                            onClickLabel = stringResource(id = OSString.itemCreation_editPicture_accessibility),
                        )
                        .background(color = Color.White) // for image with transparency
                        .testTag(tag = UiConstants.TestTag.Item.PictureButton),
                    contentScale = ContentScale.Crop,
                )
            } else if (placeHolder != null) {
                OSPlaceHolder(
                    placeholderName = placeHolder,
                    elementSize = OSSafeItemStyle.Large.elementSize,
                    placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = EmojiBackground),
                    placeholderTextStyle = OSSafeItemStyle.Large.emojiPlaceHolderTextStyle,
                    modifier = Modifier.clickable { openItemImagePickerBottomSheet() },
                )
            } else {
                OSIconButton(
                    image = OSImageSpec.Drawable(OSDrawable.ic_image),
                    onClick = openItemImagePickerBottomSheet,
                    contentDescription = LbcTextSpec.StringResource(id = OSString.itemCreation_addPicture_accessibility),
                    colors = OSIconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    buttonSize = OSDimens.SystemButtonDimension.XLarge,
                    modifier = Modifier
                        .testTag(tag = UiConstants.TestTag.Item.PictureButton)
                        .semantics { drawableId = OSDrawable.ic_image },
                )
            }
        }

        Surface(
            modifier = Modifier
                .padding(
                    start = OSDimens.SystemRoundContainerDimension.XLarge.dp // End of the large button
                        - OSDimens.SystemRoundContainerDimension.FloatingAction.dp // Minus the size of the floating action
                        + OSDimens.SystemSpacing.Small, // Plus a small padding
                ),
            shadowElevation = OSDimens.Elevation.TopButtonElevation,
            shape = CircleShape,
        ) {
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_add_photo),
                onClick = openItemImagePickerBottomSheet,
                contentDescription = if (currentImage == null) {
                    LbcTextSpec.StringResource(id = OSString.itemCreation_addPicture_accessibility)
                } else {
                    LbcTextSpec.StringResource(id = OSString.itemCreation_editPicture_accessibility)
                },
                buttonSize = OSDimens.SystemButtonDimension.FloatingAction,
                colors = OSIconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .testTag(tag = UiConstants.TestTag.Item.AddPictureButton),
            )
        }
    }
}
