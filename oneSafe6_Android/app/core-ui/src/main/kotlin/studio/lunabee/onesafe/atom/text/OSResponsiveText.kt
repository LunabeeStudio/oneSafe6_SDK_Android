package studio.lunabee.onesafe.atom.text

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

@Composable
fun OSResponsiveText(
    text: LbcTextSpec,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    OSText(
        modifier = modifier
            .drawWithContent {
                if (readyToDraw) drawContent()
            },
        text = text,
        color = color,
        textAlign = textAlign,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        overflow = TextOverflow.Ellipsis,
        style = textStyle,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && textStyle.fontSize > minFontSize) {
                textStyle = textStyle.copy(
                    fontSize = textStyle.fontSize * UiConstants.Text.RatioFontSizeReductionResponsiveText,
                    lineHeight = textStyle.lineHeight * UiConstants.Text.RatioLineHeightReductionResponsiveText,
                )
            } else {
                readyToDraw = true
            }
        },
    )
}

@Preview
@Composable
private fun PreviewOSResponsiveTextWithoutResizing() {
    OSPreviewOnSurfaceTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            OSResponsiveText(
                text = loremIpsumSpec(2),
                minFontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier
                    .width(200.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
            OSResponsiveText(
                text = loremIpsumSpec(4),
                minFontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier
                    .width(200.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
            OSResponsiveText(
                text = loremIpsumSpec(8),
                minFontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier
                    .width(200.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
