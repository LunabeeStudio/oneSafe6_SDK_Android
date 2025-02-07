package studio.lunabee.onesafe.window

import android.view.Window
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWindow: ProvidableCompositionLocal<Window> = staticCompositionLocalOf { error("no provided") }
