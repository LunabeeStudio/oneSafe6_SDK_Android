package studio.lunabee.onesafe.ui.extensions

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import com.lunabee.lblogger.LBLogger

private val logger = LBLogger.get("ScrollableState")

// https://stackoverflow.com/a/71333875/10935947
fun ScrollableState.isLastLazyItemVisible(): Boolean {
    return when (this) {
        is LazyListState -> isLastLazyItemVisible()
        is LazyGridState -> false // not needed
        is ScrollState -> false // not lazy
        else -> {
            logger.e("Unhandled scrollable state ${this.javaClass.simpleName}")
            false
        }
    }
}

private fun LazyListState.isLastLazyItemVisible(): Boolean {
    val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastItem == null || lastItem.size + lastItem.offset < layoutInfo.viewportEndOffset
}
