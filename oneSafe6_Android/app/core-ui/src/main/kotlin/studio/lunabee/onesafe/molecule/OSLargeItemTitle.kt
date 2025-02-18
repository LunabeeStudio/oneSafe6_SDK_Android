package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSResponsiveText
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSLargeItemTitle(
    title: LbcTextSpec,
    icon: OSItemIllustration,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .composed {
                val titleStr = title.string
                clearAndSetSemantics {
                    contentDescription = titleStr
                }
            }
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon.ImageComposable(contentDescription = null, style = OSSafeItemStyle.Large)
        OSRegularSpacer()

        OSResponsiveText(
            text = title,
            minFontSize = OSDimens.SystemTextSize.TitleMedium,
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 3,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSLargeItemTitlePreview() {
    OSTheme {
        Surface {
            OSLargeItemTitle(
                title = loremIpsumSpec(2),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSLargeItemTitleNoIconPreview() {
    OSTheme {
        Surface {
            OSLargeItemTitle(
                title = loremIpsumSpec(2),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSLargeItemTitleLongTitlePreview() {
    OSTheme {
        Surface {
            OSLargeItemTitle(
                title = loremIpsumSpec(2),
                icon = OSItemIllustration.Text(LbcTextSpec.Raw("L"), null),
            )
        }
    }
}
