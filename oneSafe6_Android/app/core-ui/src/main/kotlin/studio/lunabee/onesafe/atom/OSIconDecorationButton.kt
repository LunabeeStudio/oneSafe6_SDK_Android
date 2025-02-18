package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.molecule.OSRoundImage
import studio.lunabee.onesafe.ui.res.OSDimens

/**
 * [OSRoundImage] that should be used in any type of [androidx.compose.material3.Button] for decoration.
 * Default parameters are based on the most common case.
 * Color of the image itself will be handled by [androidx.compose.material3.ButtonDefaults].
 * No [Modifier] is accepted as a parameter. Use your own [Composable].
 *
 * @param image image to display.
 * @param imageSize size of the image itself.
 * @param containerSize size of the round colored around the image.
 * @param backgroundColor color apply to the rounded surface around the image.
 */
@Composable
fun OSIconDecorationButton(
    image: OSImageSpec,
    imageSize: OSDimens.SystemImageDimension = OSDimens.SystemImageDimension.Small,
    containerSize: OSDimens.SystemRoundContainerDimension = OSDimens.SystemRoundContainerDimension.Small,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    OSRoundImage(
        image = image,
        systemImageDimension = imageSize,
        contentDescription = null,
        containerColor = backgroundColor,
        modifier = Modifier
            .size(size = containerSize.dp),
    )
}

/**
 * Similar to [OSIconDecorationButton] with alert style
 *
 * @see OSIconDecorationButton
 */
@Composable
fun OSIconAlertDecorationButton(
    image: OSImageSpec,
    imageSize: OSDimens.SystemImageDimension = OSDimens.SystemImageDimension.Small,
    containerSize: OSDimens.SystemRoundContainerDimension = OSDimens.SystemRoundContainerDimension.Small,
    backgroundColor: Color = MaterialTheme.colorScheme.error,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onError) {
        OSRoundImage(
            image = image,
            systemImageDimension = imageSize,
            contentDescription = null,
            containerColor = backgroundColor,
            modifier = Modifier
                .size(size = containerSize.dp),
        )
    }
}
