package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ItemFormHeader(
    currentImage: OSImageSpec?,
    placeHolder: LbcTextSpec?,
    loadingProgress: Float?,
    openColorPickerBottomSheet: () -> Unit,
    openItemImagePickerBottomSheet: () -> Unit,
) {
    val accessibilityState: AccessibilityState = rememberOSAccessibilityState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        contentAlignment = Alignment.Center,
    ) {
        ItemImageChoice(
            currentImage = currentImage,
            openItemImagePickerBottomSheet = openItemImagePickerBottomSheet,
            loadingProgress = loadingProgress,
            placeHolder = placeHolder,
        )

        // Color picker currently used is not made for accessibility.
        if (!accessibilityState.isTouchExplorationEnabled) {
            Box(
                modifier = Modifier
                    .padding(bottom = OSDimens.External.DefaultCircularStrokeWidth)
                    .align(alignment = Alignment.BottomEnd),
            ) {
                OSIconButton(
                    image = OSImageSpec.Drawable(OSDrawable.ic_paint),
                    onClick = openColorPickerBottomSheet,
                    buttonSize = OSDimens.SystemButtonDimension.FloatingAction,
                    contentDescription = LbcTextSpec.StringResource(OSString.itemCreation_colorChoice_button_accessibility),
                    modifier = Modifier
                        .testTag(tag = UiConstants.TestTag.Item.ColorPickerButton),
                    colors = OSIconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }
    }
}
