package studio.lunabee.onesafe.atom.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun OSRowLabel(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    color: Color = LocalDesignSystem.current.rowLabelColor,
) {
    OSText(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
        maxLines = UiConstants.Text.MaxLineSize,
    )
}

@Composable
internal fun OSHeaderRowLabel(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    color: Color = LocalDesignSystem.current.rowTextColor,
) {
    OSText(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
        maxLines = UiConstants.Text.MaxLineSize,
    )
}

@Composable
internal fun OSRowText(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
    maxLines: Int = UiConstants.Text.MaxLineSize,
    color: Color = LocalDesignSystem.current.rowTextColor,
) {
    val textStyle = MaterialTheme.typography.bodyLarge.let { defaultFont ->
        fontFamily?.let { font -> defaultFont.copy(fontFamily = font) } ?: defaultFont
    }
    OSText(
        text = text,
        style = textStyle,
        color = color,
        maxLines = maxLines,
        modifier = modifier,
    )
}

@Composable
internal fun OSRowSecondaryText(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    color: Color = LocalDesignSystem.current.rowSecondaryColor,
    maxLines: Int = UiConstants.Text.MaxLineSize,
) {
    OSText(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
    )
}

@Composable
@OsDefaultPreview
private fun OSRowLabelPreview() {
    OSPreviewOnSurfaceTheme {
        OSRowLabel(text = loremIpsumSpec(2))
    }
}

@Composable
@OsDefaultPreview
private fun OSHeaderRowLabelPreview() {
    OSPreviewOnSurfaceTheme {
        OSHeaderRowLabel(text = loremIpsumSpec(2))
    }
}

@Composable
@OsDefaultPreview
private fun OSRowTextPreview() {
    OSPreviewOnSurfaceTheme {
        OSRowText(text = loremIpsumSpec(2))
    }
}

@Composable
@OsDefaultPreview
private fun OSRowSecondaryTextPreview() {
    OSPreviewOnSurfaceTheme {
        OSRowSecondaryText(text = loremIpsumSpec(2))
    }
}
