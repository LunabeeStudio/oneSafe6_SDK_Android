package studio.lunabee.onesafe.common.extensions

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope

val LazyGridScope.MaxLineSpan: LazyGridItemSpanScope.() -> GridItemSpan
    get() = { GridItemSpan(currentLineSpan = maxLineSpan) }
