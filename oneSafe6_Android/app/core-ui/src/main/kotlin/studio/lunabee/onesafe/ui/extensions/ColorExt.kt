package studio.lunabee.onesafe.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColorInt
import androidx.palette.graphics.Palette
import java.util.Locale

@Composable
fun Int.toColor(): Color {
    return Color(LocalContext.current.resources.getColor(this, LocalContext.current.theme))
}

fun String.toColor(): Color {
    return Color("#$this".toColorInt())
}

/**
 * Get color without "#" to align with iOS.
 */
val Color.hexValue: String
    get() = String.format(locale = Locale.ROOT, format = "%08X", toArgb())

/**
 * Extract first color available generated by [Palette.generate]. Not all colors are generated depending on image.
 */
fun Palette.getFirstColorGenerated(): Color? {
    return (
        vibrantSwatch
            ?: darkVibrantSwatch
            ?: lightVibrantSwatch
            ?: mutedSwatch
            ?: darkMutedSwatch
            ?: lightMutedSwatch
        )
        ?.rgb?.let { rgb ->
            Color(color = rgb)
        }
}
