package studio.lunabee.onesafe.animation

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import studio.lunabee.onesafe.ui.extensions.isLastLazyItemVisible
import studio.lunabee.onesafe.ui.res.OSDimens

class OSTopBarVisibilityNestedScrollConnection(
    private val scrollableState: ScrollableState,
    private val topBarScrollVisibilityThresholdPx: Float,
) : NestedScrollConnection {
    var isTopBarVisible: Boolean by mutableStateOf(value = true)
        private set

    private var deltaAccumulator = 0f

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (!scrollableState.isLastLazyItemVisible()) {
            val delta = consumed.y
            when {
                isTopBarVisible && deltaAccumulator <= -topBarScrollVisibilityThresholdPx -> {
                    isTopBarVisible = false
                    deltaAccumulator = 0f
                }
                !isTopBarVisible && deltaAccumulator >= topBarScrollVisibilityThresholdPx -> {
                    isTopBarVisible = true
                    deltaAccumulator = 0f
                }
                (isTopBarVisible && delta < 0) || (!isTopBarVisible && delta > 0) -> {
                    deltaAccumulator += delta
                }
            }
        }

        return Offset.Zero
    }
}

@Composable
fun rememberOSTopBarVisibilityNestedScrollConnection(
    scrollableState: ScrollableState,
): OSTopBarVisibilityNestedScrollConnection {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val isLastItemVisible by remember(scrollableState) {
        derivedStateOf { scrollableState.isLastLazyItemVisible() }
    }

    val topBarScrollVisibilityThresholdPx = with(LocalDensity.current) {
        OSDimens.ItemTopBar.TopBarScrollVisibilityThreshold.toPx()
    }

    return remember(scrollableState, isLastItemVisible, isImeVisible) {
        OSTopBarVisibilityNestedScrollConnection(scrollableState, topBarScrollVisibilityThresholdPx)
    }
}
