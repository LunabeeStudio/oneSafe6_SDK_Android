package studio.lunabee.onesafe.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy

/**
 * CompositionLocal that configures the lambda to call on any touch events for composable outside the activity window (dialog, popup)
 */
val LocalOnTouchWindow: ProvidableCompositionLocal<() -> Unit> = staticCompositionLocalOf { {} }

/**
 * A [Box] which listen pointer events and call the provided [LocalOnTouchWindow] on every events
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchInterceptorBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val onTouch = LocalOnTouchWindow.current
    Box(
        modifier.motionEventSpy {
            onTouch()
        },
    ) {
        content()
    }
}
