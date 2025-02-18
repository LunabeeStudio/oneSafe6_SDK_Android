package studio.lunabee.onesafe.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun OSTopBarAnimatedVisibility(visible: Boolean, content: @Composable () -> Unit) {
    val slideOffset = with(LocalDensity.current) { (-100).dp.roundToPx() } // arbitrary value

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { slideOffset } + fadeIn(),
        exit = slideOutVertically { slideOffset } + fadeOut(),
    ) {
        content()
    }
}
