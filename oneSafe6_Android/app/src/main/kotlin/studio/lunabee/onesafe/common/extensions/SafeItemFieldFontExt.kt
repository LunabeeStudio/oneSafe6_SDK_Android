package studio.lunabee.onesafe.common.extensions

import androidx.compose.ui.text.font.FontFamily
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldFont
import studio.lunabee.onesafe.ui.theme.OSTypography

fun SafeItemFieldFont.toFontFamily(): FontFamily? = when (this) {
    SafeItemFieldFont.Default -> null
    SafeItemFieldFont.Legibility -> OSTypography.Legibility
}
