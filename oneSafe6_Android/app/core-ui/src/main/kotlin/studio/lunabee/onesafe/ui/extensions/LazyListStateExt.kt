package studio.lunabee.onesafe.ui.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LazyListState.topAppBarElevation: Dp
    get() = if (firstVisibleItemIndex == 0) {
        minOf(firstVisibleItemScrollOffset.toFloat().dp, MaterialAppBarDefaultsTopAppBarElevation)
    } else {
        MaterialAppBarDefaultsTopAppBarElevation
    }

// TODO migrate top bar to match m3. Meanwhile, use m2 elevation
//  https://developer.android.com/jetpack/compose/designsystems/material2-material3#m2_11
val MaterialAppBarDefaultsTopAppBarElevation: Dp = 4.dp
