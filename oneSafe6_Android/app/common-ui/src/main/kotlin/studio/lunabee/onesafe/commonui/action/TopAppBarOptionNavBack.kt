package studio.lunabee.onesafe.commonui.action

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav

fun topAppBarOptionNavBack(
    navigateBack: () -> Unit,
    isEnabled: Boolean = true,
    image: OSImageSpec = OSImageSpec.Drawable(OSDrawable.ic_back),
): TopAppBarOptionNav = TopAppBarOptionNav(
    image = image,
    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
    onClick = navigateBack,
    state = if (isEnabled) OSActionState.Enabled else OSActionState.Disabled,
)
