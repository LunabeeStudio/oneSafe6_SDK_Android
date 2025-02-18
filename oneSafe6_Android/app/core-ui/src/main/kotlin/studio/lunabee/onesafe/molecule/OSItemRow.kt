package studio.lunabee.onesafe.molecule

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall

@Composable
fun OSItemRow(
    osItemIllustration: OSItemIllustration,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    contentDescription: LbcTextSpec? = label,
    subtitle: LbcTextSpec? = null,
    itemSubtitleMaxLine: Int = ItemSubTitleMaxLine,
) {
    Row(
        modifier = modifier
            .composed {
                val text = "${contentDescription?.string} ${subtitle?.string.orEmpty()}".trim()
                clearAndSetSemantics {
                    this.text = AnnotatedString(text)
                }
            }
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        osItemIllustration.ImageComposable(contentDescription = contentDescription, style = OSSafeItemStyle.Small)
        Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
        Column {
            OSText(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = ItemTitleMaxLine,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) { // TODO removed isNotEmpty check, should be handle before
                OSText(
                    text = subtitle,
                    style = MaterialTheme.typography.labelXSmall,
                    color = LocalColorPalette.current.Neutral60,
                    maxLines = itemSubtitleMaxLine,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private const val ItemTitleMaxLine: Int = 2
const val ItemSubTitleMaxLine: Int = 1

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun SearchItemPreview() {
    OSPreviewBackgroundTheme {
        OSItemRow(
            osItemIllustration = OSItemIllustration.Text(LbcTextSpec.Raw("G"), null),
            label = LbcTextSpec.Raw("Gmail"),
            paddingValues = PaddingValues(0.dp),
        )
    }
}

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun SearchItemWithIdentifierPreview() {
    OSPreviewBackgroundTheme {
        OSItemRow(
            osItemIllustration = OSItemIllustration.Text(LbcTextSpec.Raw("G"), null),
            label = LbcTextSpec.Raw("Gmail"),
            subtitle = LbcTextSpec.Raw("test@lunabee.com"),
            paddingValues = PaddingValues(0.dp),
        )
    }
}
