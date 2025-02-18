package studio.lunabee.onesafe.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import studio.lunabee.onesafe.ui.UiConstants

object OSShimmerTheme {
    val Theme: ShimmerTheme = defaultShimmerTheme.copy(
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = UiConstants.Shimmer.AnimDurationMs,
                delayMillis = UiConstants.Shimmer.AnimDelayMs,
                easing = LinearEasing,
            ),
        ),
        shaderColors = listOf(
            Color.Unspecified.copy(alpha = 0.45f),
            Color.Unspecified.copy(alpha = 1.00f),
            Color.Unspecified.copy(alpha = 0.45f),
        ),
    )
}
