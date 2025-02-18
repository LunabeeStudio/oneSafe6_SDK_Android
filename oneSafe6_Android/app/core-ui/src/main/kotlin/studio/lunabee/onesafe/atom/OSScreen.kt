package studio.lunabee.onesafe.atom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun OSScreen(
    testTag: String,
    modifier: Modifier = Modifier,
    background: Brush = LocalDesignSystem.current.backgroundGradient(),
    applySystemBarPadding: Boolean = true,
    content: @Composable (BoxScope.() -> Unit),
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxSize()
            .drawBehind { drawRect(brush = background) }
            .then(
                if (applySystemBarPadding) {
                    Modifier.osSystemBarsPadding()
                } else {
                    Modifier
                },
            )
            .then(modifier),
        content = content,
    )
}

private fun Modifier.osSystemBarsPadding(): Modifier = composed {
    val orientation = LocalConfiguration.current.orientation
    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this
            .statusBarsPadding()
            .landscapeSystemBarsPadding(orientation)
    } else {
        this
            .statusBarsPadding()
    }
}
