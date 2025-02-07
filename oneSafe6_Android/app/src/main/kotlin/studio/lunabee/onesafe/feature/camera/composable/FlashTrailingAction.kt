package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.ui.res.OSDimens

fun flashTrailingAction(
    isFlashEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
): TopAppBarOptionTrailing = TopAppBarOptionTrailing {
    OSIconButton(
        image = if (isFlashEnabled) {
            OSImageSpec.Drawable(OSDrawable.ic_flash)
        } else {
            OSImageSpec.Drawable(OSDrawable.ic_flash_off)
        },
        onClick = onClick,
        colors = if (isFlashEnabled) {
            OSIconButtonDefaults.primaryIconButtonColors()
        } else {
            OSIconButtonDefaults.secondaryIconButtonColors()
        },
        modifier = modifier,
        buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
        contentDescription = if (isFlashEnabled) {
            LbcTextSpec.StringResource(OSString.accessibility_camera_disableFlash)
        } else {
            LbcTextSpec.StringResource(OSString.accessibility_camera_enableFlash)
        },
    )
}
