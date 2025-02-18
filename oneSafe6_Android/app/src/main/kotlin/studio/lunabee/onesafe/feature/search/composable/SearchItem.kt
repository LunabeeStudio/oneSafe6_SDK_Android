package studio.lunabee.onesafe.feature.search.composable

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
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
fun SearchItem(
    osItemIllustration: OSItemIllustration,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    contentDescription: LbcTextSpec? = label,
    onClick: () -> Unit,
    identifier: LbcTextSpec? = null,
) {
    Row(
        modifier = modifier
            .composed {
                val text = "${contentDescription?.string} ${identifier?.string.orEmpty()}".trim()
                clearAndSetSemantics {
                    this.text = AnnotatedString(text)
                }
            }
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(paddingValues)
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        osItemIllustration.ImageComposable(contentDescription = contentDescription, style = OSSafeItemStyle.Small)
        Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
        Column {
            OSText(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = ItemTitleMaxLine,
                overflow = TextOverflow.Ellipsis,
            )
            if (identifier != null) { // TODO removed isNotEmpty check, should be handle before
                OSText(
                    text = identifier,
                    style = MaterialTheme.typography.labelXSmall,
                    color = LocalColorPalette.current.Neutral60,
                    maxLines = ItemSubTitleMaxLine,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private const val ItemTitleMaxLine: Int = 2
private const val ItemSubTitleMaxLine: Int = 1

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun SearchItemPreview() {
    OSPreviewBackgroundTheme {
        SearchItem(
            osItemIllustration = OSItemIllustration.Text(LbcTextSpec.Raw("G"), null),
            label = LbcTextSpec.Raw("Gmail"),
            paddingValues = PaddingValues(0.dp),
            onClick = { },
        )
    }
}

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun SearchItemWithIdentifierPreview() {
    OSPreviewBackgroundTheme {
        SearchItem(
            osItemIllustration = OSItemIllustration.Text(LbcTextSpec.Raw("G"), null),
            label = LbcTextSpec.Raw("Gmail"),
            identifier = LbcTextSpec.Raw("test@lunabee.com"),
            paddingValues = PaddingValues(0.dp),
            onClick = { },
        )
    }
}
