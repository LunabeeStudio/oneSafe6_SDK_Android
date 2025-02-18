package studio.lunabee.onesafe.feature.camera.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun CameraOptionButton(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    contentDescription: LbcTextSpec,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(OSDimens.Camera.optionButtonSize)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                role = Role.Button,
                onClickLabel = contentDescription.string,
            )
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(
                width = OSDimens.SystemSpacing.ExtraSmall,
                color = LocalContentColor.current,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        OSImage(
            image = OSImageSpec.Drawable(icon),
            modifier = Modifier.size(OSDimens.Camera.optionButtonIconSize),
        )
    }
}
