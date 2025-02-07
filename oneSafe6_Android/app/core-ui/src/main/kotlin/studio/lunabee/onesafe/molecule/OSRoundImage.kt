package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun OSRoundImage(
    image: OSImageSpec,
    contentDescription: LbcTextSpec?,
    modifier: Modifier = Modifier,
    systemImageDimension: OSDimens.SystemImageDimension = OSDimens.SystemImageDimension.Undefined,
    // Background that will be used for OSImageSpec.Drawable or other image without background.
    containerColor: Color = Color.White,
) {
    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = containerColor),
        contentAlignment = Alignment.Center,
    ) {
        OSImage(
            image = image,
            contentDescription = contentDescription,
            modifier = if (systemImageDimension == OSDimens.SystemImageDimension.Undefined) {
                Modifier
                    .fillMaxSize()
            } else {
                Modifier
                    .size(size = systemImageDimension.dp)
            },
        )
    }
}
