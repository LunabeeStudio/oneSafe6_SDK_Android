package studio.lunabee.onesafe.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.google.android.material.color.DynamicColors
import studio.lunabee.compose.theme.LbcThemeUtilities
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun OSTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    isMaterialYouSettingsEnabled: Boolean = true,
    colorPalette: OSColorPalette = MainOSColorPalette,
    lightColorScheme: ColorScheme = LbcThemeUtilities.getMaterialColorSchemeFromColor(colorPalette.Primary30, false),
    darkColorScheme: ColorScheme = LbcThemeUtilities.getMaterialColorSchemeFromColor(colorPalette.Primary30, true),
    content: @Composable () -> Unit,
) {
    val isMaterialYouEnabled = !LocalInspectionMode.current && DynamicColors.isDynamicColorAvailable() && isMaterialYouSettingsEnabled

    @Suppress("NewApi") // Already check by isDynamicColorAvailable
    val colorScheme = when {
        isMaterialYouEnabled && isSystemInDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        isMaterialYouEnabled -> dynamicLightColorScheme(LocalContext.current)
        isSystemInDarkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    CompositionLocalProvider(
        LocalDesignSystem provides OSDesignSystem(isMaterialYouEnabled, isSystemInDarkTheme = isSystemInDarkTheme),
        LocalContentColor provides colorScheme.onSurface,
        LocalColorPalette provides colorPalette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OSTypography.Typography,
            shapes = Shapes(
                extraLarge = RoundedCornerShape(size = OSDimens.SystemCornerRadius.ExtraLarge),
                medium = RoundedCornerShape(size = OSDimens.SystemCornerRadius.Regular),
                small = RoundedCornerShape(size = OSDimens.SystemCornerRadius.Small),
            ),
            content = content,
        )
    }
}

@Composable
fun OSPreviewBackgroundTheme(content: @Composable () -> Unit) {
    OSTheme {
        Box(
            modifier = Modifier
                .padding(30.dp)
                .background(LocalDesignSystem.current.backgroundGradient()),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
fun OSPreviewOnSurfaceTheme(content: @Composable () -> Unit) {
    OSTheme {
        Surface {
            content()
        }
    }
}
