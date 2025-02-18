package studio.lunabee.onesafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.theme.LbcThemeUtilities

@Composable
fun OSUserTheme(
    customPrimaryColor: Color?,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        // Provides new LocalDesignSystem if needed. If user set a custom color, use the "MaterialYou experience".
        LocalDesignSystem provides OSDesignSystem(
            isMaterialYouEnabled = customPrimaryColor != null || LocalDesignSystem.current.isMaterialYouEnabled,
            isSystemInDarkTheme = isSystemInDarkTheme(),
        ),
    ) {
        MaterialTheme(
            colorScheme = customPrimaryColor?.let {
                LbcThemeUtilities.getMaterialColorSchemeFromColor(color = it, isInDarkMode = isSystemInDarkTheme())
            } ?: MaterialTheme.colorScheme,
            content = content,
        )
    }
}
