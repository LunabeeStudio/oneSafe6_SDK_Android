package studio.lunabee.onesafe.molecule

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.preview.PreviewGroup
import studio.lunabee.onesafe.ui.theme.OSTheme
import kotlin.random.Random

@Composable
fun OSSafeItem(
    illustration: OSItemIllustration,
    style: OSSafeItemStyle,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    contentDescription: LbcTextSpec? = label,
    labelMinLines: Int = 1,
) {
    Column(
        modifier = modifier
            .requiredWidth(style.elementSize)
            .composed {
                val text = contentDescription?.string.orEmpty()
                clearAndSetSemantics { this.text = AnnotatedString(text) }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration.ImageComposable(contentDescription = contentDescription, style = style)

        Spacer(modifier = Modifier.padding(top = style.paddingWithLabel))

        val labelString = label.string
        val wordCount = labelString.split("\\s+".toRegex()).size

        var fixedLabel by remember(labelString) { mutableStateOf(labelString) }
        var readyToDraw by remember(labelString) { mutableStateOf(false) }

        // FIXME Do not use framework ellipsize https://issuetracker.google.com/issues/240060683
        OSText(
            text = LbcTextSpec.Raw(fixedLabel),
            modifier = Modifier
                .testTag(UiConstants.TestTag.OSSafeItemText)
                .drawWithContent { if (readyToDraw) drawContent() },
            style = style.labelTextStyle,
            minLines = labelMinLines,
            maxLines = 2,
            textAlign = TextAlign.Center,
            onTextLayout = { layoutResult ->
                val charToRemove = 1
                if (
                    wordCount == 1 &&
                    layoutResult.lineCount == 2 &&
                    layoutResult.getLineEnd(1) - layoutResult.getLineStart(1) < 3
                ) {
                    // Avoid word cut with less than 3 chars
                    val keepCharCount = layoutResult.getLineStart(1) - charToRemove
                    fixedLabel = fixedLabel.take(keepCharCount) + Typography.ellipsis
                } else if (
                    layoutResult.lineCount == 2 &&
                    layoutResult.hasVisualOverflow
                ) {
                    // Manually add ellipsize on second line
                    val endIndex = layoutResult.getLineEnd(1, visibleEnd = true) - charToRemove
                    fixedLabel = fixedLabel.substring(0, endIndex) + Typography.ellipsis
                } else {
                    readyToDraw = true
                }
            },
        )
    }
}

@Preview
@Composable
private fun ImageOSSafeItemPreview() {
    OSTheme {
        OSSafeItem(
            illustration = OSItemIllustration.Image(OSImageSpec.Data(iconSample)),
            style = OSSafeItemStyle.Regular,
            label = loremIpsumSpec(Random.nextInt(1, 3)),
        )
    }
}

@Preview
@Composable
private fun TextOSSafeItemPreview() {
    OSTheme {
        OSSafeItem(
            illustration = OSItemIllustration.Text(
                loremIpsumSpec(words = 1),
                randomColor,
            ),
            style = OSSafeItemStyle.Regular,
            label = loremIpsumSpec(Random.nextInt(1, 3)),
        )
    }
}

@Preview
@Composable
private fun IconOSSafeItemPreview() {
    OSTheme {
        OSSafeItem(
            illustration = OSItemIllustration.Image(
                image = OSImageSpec.Drawable(drawable = R.drawable.os_ic_sample, tintColor = randomColor),
            ),
            style = OSSafeItemStyle.Regular,
            label = loremIpsumSpec(Random.nextInt(1, 3)),
        )
    }
}

@Preview(name = "Small safe item", group = PreviewGroup.SafeItem)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Small safe item",
    group = PreviewGroup.SafeItem,
)
@Composable
private fun SmallOSSafeItemPreview() {
    OSTheme {
        OSSafeItem(
            illustration = OSItemIllustration.Text(
                loremIpsumSpec(words = 1),
                randomColor,
            ),
            style = OSSafeItemStyle.Small,
            label = loremIpsumSpec(Random.nextInt(1, 3)),
        )
    }
}

@Preview(name = "Large safe item", group = PreviewGroup.SafeItem)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Large safe item",
    group = PreviewGroup.SafeItem,
)
@Composable
private fun LargeOSSafeItemPreview() {
    OSTheme {
        OSSafeItem(
            illustration = OSItemIllustration.Text(
                loremIpsumSpec(words = 1),
                randomColor,
            ),
            style = OSSafeItemStyle.Large,
            label = loremIpsumSpec(Random.nextInt(1, 3)),
        )
    }
}
