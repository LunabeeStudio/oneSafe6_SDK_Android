package studio.lunabee.onesafe.molecule

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.preview.PreviewGroup
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.titleLargeBlack

@Composable
fun OSSectionHeader(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLargeBlack,
    actionButton: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OSText(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f),
            style = textStyle,
        )

        actionButton?.invoke(this)
    }
}

@Preview(name = "Section header with action", group = PreviewGroup.Text.SectionHeader)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Section header with action",
    group = PreviewGroup.Text.SectionHeader,
)
@Composable
private fun OSSectionHeaderActionPreview() {
    OSTheme {
        OSSectionHeader(
            text = loremIpsumSpec(1),
        ) {
            OSTextButton(
                text = loremIpsumSpec(1),
                onClick = { },
            )
        }
    }
}

@Preview(name = "Section header", group = PreviewGroup.Text.SectionHeader)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Section header",
    group = PreviewGroup.Text.SectionHeader,
)
@Composable
private fun OSSectionHeaderPreview() {
    OSTheme {
        OSSectionHeader(
            text = loremIpsumSpec(1),
        )
    }
}
