package studio.lunabee.onesafe.extension

import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp

/**
 * In landscape mode, navigation bar & cutout may be position on left or right of the screen.
 */
fun Modifier.landscapeSystemBarsPadding(pOrientation: Int? = null): Modifier {
    return composed {
        val orientation = pOrientation ?: LocalConfiguration.current.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Left))
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Right))
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Left))
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Right))
        } else {
            this
        }
    }
}

/**
 * Add a shadow elevation to a composable.
 */
fun Modifier.osShadowElevation(elevation: Dp): Modifier {
    return graphicsLayer {
        this.shadowElevation = elevation.value
        this.clip = false
        scaleX = TopBarScale
        scaleY = TopBarScale
    }
}

// Used so that the horizontal and top shadows are not displayed when need to elevate to top bar
private const val TopBarScale: Float = 1.01f
