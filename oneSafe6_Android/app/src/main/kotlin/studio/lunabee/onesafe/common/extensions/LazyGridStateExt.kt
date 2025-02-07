package studio.lunabee.onesafe.common.extensions

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.onesafe.ui.res.OSDimens

val LazyGridState.topAppBarElevation: Dp
    get() = if (firstVisibleItemIndex == 0) {
        minOf(firstVisibleItemScrollOffset.toFloat().dp, OSDimens.Elevation.TopAppBarElevation)
    } else {
        OSDimens.Elevation.TopAppBarElevation
    }
