package studio.lunabee.onesafe.commonui.action

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.textfield.OSTrailingAction
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun VisibilityTrailingAction(
    isSecuredVisible: Boolean,
    onClick: () -> Unit,
    contentDescription: LbcTextSpec?,
    modifier: Modifier = Modifier,
    tintColor: Color? = null,
) {
    val drawableRes = if (isSecuredVisible) {
        OSDrawable.ic_visibility_off
    } else {
        OSDrawable.ic_visibility_on
    }

    OSTrailingAction(
        image = OSImageSpec.Drawable(drawable = drawableRes, tintColor = tintColor),
        onClick = onClick,
        contentDescription = contentDescription,
        modifier = modifier,
        testTag = UiConstants.TestTag.Item.VisibilityAction,
    )
}
