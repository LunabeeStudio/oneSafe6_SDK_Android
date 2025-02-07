package studio.lunabee.onesafe.ui.extensions

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.unit.Dp
import studio.lunabee.onesafe.ui.res.OSDimens

val ScrollState.topAppBarElevation: Dp
    get() = if (value == 0) {
        OSDimens.Elevation.None
    } else {
        MaterialAppBarDefaultsTopAppBarElevation
    }
