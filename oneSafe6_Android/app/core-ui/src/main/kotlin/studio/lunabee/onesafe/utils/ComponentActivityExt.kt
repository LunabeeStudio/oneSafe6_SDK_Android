package studio.lunabee.onesafe.utils

import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

fun ComponentActivity.oSDefaultEnableEdgeToEdge(
    forceLight: Boolean = false,
) {
    val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    // Don't use SystemBarStyle.auto for navigation bar because it always add a scrim (cf doc)
    val navigationBarStyle = if (isDark && !forceLight) {
        SystemBarStyle.dark(scrim = Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(scrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT)
    }
    val statusBarStyle = if (forceLight) {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    } else {
        SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(
        statusBarStyle = statusBarStyle,
        navigationBarStyle = navigationBarStyle,
    )
}
