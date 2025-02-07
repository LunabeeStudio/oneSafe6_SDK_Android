package studio.lunabee.onesafe.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import studio.lunabee.compose.theme.LbcThemeUtilities
import studio.lunabee.onesafe.ui.extensions.toColor
import studio.lunabee.onesafe.ui.res.OSDimens

class OSDesignSystem(
    val isMaterialYouEnabled: Boolean,
    val isSystemInDarkTheme: Boolean,
) {
    @Composable
    fun backgroundGradient(): Brush {
        val isSystemInDarkTheme = isSystemInDarkTheme
        val startColor = if (isSystemInDarkTheme) {
            getDarkStartColor(MaterialTheme.colorScheme.primary)
        } else {
            getLightStartColor(LocalColorPalette.current)
        }
        return Brush.linearGradient(0f to startColor, 0.33f to backgroundEndColor)
    }

    @Composable
    fun warningBackgroundGradient(): Brush {
        val palette = LocalColorPalette.current
        return if (isSystemInDarkTheme) {
            Brush.linearGradient(0f to palette.Warning90, 0.33f to palette.Warning95)
        } else {
            Brush.linearGradient(0f to palette.Warning02, 0.33f to palette.Warning05)
        }
    }

    @Composable
    fun feedbackWarningBackgroundGradient(): Pair<Color, Brush> {
        val palette = LocalColorPalette.current
        // TODO <theming> implement material you?
        return Color.White to Brush.linearGradient(listOf(palette.FeedbackWarningStart, palette.FeedbackWarningEnd))
    }

    @Composable
    fun getBackgroundGradientStartColor(): Color {
        return if (isSystemInDarkTheme) {
            getDarkStartColor(MaterialTheme.colorScheme.primary)
        } else {
            getLightStartColor(LocalColorPalette.current)
        }
    }

    @Composable
    fun getBackgroundGradientEndColor(): Color {
        return backgroundEndColor
    }

    fun getDarkStartColor(colorSeed: Color): Color {
        return LbcThemeUtilities.getToneForColor(color = colorSeed, tone = 10)
    }

    fun getLightStartColor(palette: OSColorPalette): Color {
        return palette.Neutral00
    }

    private val backgroundEndColor: Color
        @Composable
        get() = getEndColor(MaterialTheme.colorScheme.primary)

    fun getEndColor(colorSeed: Color): Color = if (isSystemInDarkTheme) {
        LbcThemeUtilities.getToneForColor(color = colorSeed, tone = 20)
    } else {
        LbcThemeUtilities.getToneForColor(color = colorSeed, tone = 93)
    }

    @Composable
    fun disabledSearchButtonColor(): Pair<Color, Color> {
        val disabledSearchColorBackground = if (isSystemInDarkTheme) {
            MaterialTheme.colorScheme.primary
        } else {
            LbcThemeUtilities.getToneForColor(
                color = MaterialTheme.colorScheme.primary,
                tone = 95,
            )
        }
        val disabledSearchColorContent = if (isSystemInDarkTheme) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.primary
        }
        return disabledSearchColorBackground to disabledSearchColorContent
    }

    @Composable
    fun bubblesBackGround(): Brush {
        val isSystemInDarkTheme = isSystemInDarkTheme
        val color = if (isSystemInDarkTheme) {
            LbcThemeUtilities.getToneForColor(color = MaterialTheme.colorScheme.primary, tone = 5)
        } else {
            LbcThemeUtilities.getToneForColor(color = MaterialTheme.colorScheme.primary, tone = 95)
        }
        return SolidColor(color)
    }

    @Composable
    fun bubblesSecondaryContainer(): Color {
        val isSystemInDarkTheme = isSystemInDarkTheme
        return if (isSystemInDarkTheme) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            LbcThemeUtilities.getToneForColor(color = MaterialTheme.colorScheme.primary, tone = 90)
        }
    }

    @Composable
    fun simpleBackground(): Brush {
        val isSystemInDarkTheme = isSystemInDarkTheme
        val color = if (isSystemInDarkTheme) {
            LbcThemeUtilities.getToneForColor(color = MaterialTheme.colorScheme.primary, tone = 10)
        } else {
            LocalColorPalette.current.Neutral00
        }
        return SolidColor(color)
    }

    @Composable
    @ReadOnlyComposable
    fun navigationItemLabelColor(isActive: Boolean): Color {
        val isSystemInDarkTheme = isSystemInDarkTheme
        return if (isActive) {
            LbcThemeUtilities.getToneForColor(
                color = MaterialTheme.colorScheme.primary,
                tone = if (isSystemInDarkTheme) 90 else 40,
            )
        } else {
            LocalColorPalette.current.Neutral60
        }
    }

    @Composable
    fun outlinedTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (isSystemInDarkTheme) LocalColorPalette.current.Neutral70 else LocalColorPalette.current.Neutral30,
            unfocusedLabelColor = if (isSystemInDarkTheme) LocalColorPalette.current.Neutral10 else LocalColorPalette.current.Neutral80,
        )
    }

    val scrimColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)

    val navBarOverlayBrush: Brush
        @Composable
        get() = Brush.verticalGradient(0f to Color.Transparent, 0.3f to MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))

    val navBarOverlayBackgroundGradientBrush: Brush
        @Composable
        get() = Brush.verticalGradient(
            0f to Color.Transparent,
            0.3f to LocalDesignSystem.current.backgroundEndColor.copy(alpha = 0.5f),
        )

    val tabPrimaryDisabledColor: Color
        @Composable get() {
            return when {
                isMaterialYouEnabled && isSystemInDarkTheme -> android.R.color.system_accent1_600.toColor()
                isMaterialYouEnabled -> android.R.color.system_accent1_100.toColor()
                isSystemInDarkTheme -> LocalColorPalette.current.Primary60
                else -> LocalColorPalette.current.Primary10
            }
        }

    val rowLabelColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme) {
            LocalColorPalette.current.Neutral30
        } else {
            LocalColorPalette.current.Neutral60
        }

    val rowTextColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme) {
            LocalColorPalette.current.Neutral10
        } else {
            LocalColorPalette.current.Neutral80
        }

    val rowSecondaryColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isSystemInDarkTheme) {
            LocalColorPalette.current.Neutral30
        } else {
            LocalColorPalette.current.Neutral60
        }

    internal val menuItemColors: MenuItemColors
        @Composable
        get() = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.primary,
            leadingIconColor = MaterialTheme.colorScheme.primary,
        )

    val rowClickablePaddingValues: PaddingValues
        @Composable
        get() = PaddingValues(
            vertical = OSDimens.SystemSpacing.Small,
            horizontal = ButtonDefaults.TextButtonContentPadding.calculateStartPadding(layoutDirection = LocalLayoutDirection.current),
        )

    @Composable
    fun getRowClickablePaddingValuesDependingOnIndex(index: Int, elementsCount: Int): PaddingValues {
        return with(rowClickablePaddingValues) {
            when {
                elementsCount == 1 -> PaddingValues(
                    vertical = calculateTopPadding() + OSDimens.SystemSpacing.Small,
                    horizontal = calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                )
                index == 0 -> PaddingValues(
                    top = calculateTopPadding() + OSDimens.SystemSpacing.Small,
                    bottom = calculateBottomPadding(),
                    start = calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    end = calculateEndPadding(layoutDirection = LayoutDirection.Ltr),
                )
                index == (elementsCount - 1) -> PaddingValues(
                    top = calculateTopPadding(),
                    bottom = calculateBottomPadding() + OSDimens.SystemSpacing.Small,
                    start = calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                    end = calculateEndPadding(layoutDirection = LayoutDirection.Ltr),
                )
                else -> this
            }
        }
    }
}
