package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes

@Composable
fun CameraInfoCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OSMessageCard(
        title = LbcTextSpec.StringResource(OSString.common_tips),
        description = LbcTextSpec.StringResource(OSString.cameraScreen_security_explanation),
        attributes = OSMessageCardAttributes()
            .dismissible(
                icon = OSImageSpec.Drawable(OSDrawable.ic_baseline_close),
                contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                onDismiss = onDismiss,
            ),
        modifier = modifier,
    )
}
