package studio.lunabee.onesafe.ui.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalDesignSystem: ProvidableCompositionLocal<OSDesignSystem> =
    compositionLocalOf {
        OSDesignSystem(
            isMaterialYouEnabled = true,
            isSystemInDarkTheme = false,
        )
    }
