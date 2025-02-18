package studio.lunabee.onesafe.commonui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.molecule.OSSwitchRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: LbcTextSpec? = null,
    titleTrailingLayout: @Composable (() -> Unit)? = null,
    actions: List<SettingsAction> = listOf(),
    footer: LbcTextSpec? = null,
) {
    OSCard(
        modifier = modifier,
    ) {
        title?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = OSDimens.SystemSpacing.Regular, bottom = OSDimens.SystemSpacing.ExtraSmall),
            ) {
                OSText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                )
                titleTrailingLayout?.invoke()
            }
        }
        actions.forEachIndexed { idx, action ->
            key(idx) {
                action.Composable()
            }
        }
        footer?.let {
            OSText(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Small,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun SettingsCardPreview() {
    OSTheme {
        SettingsCard(
            title = loremIpsumSpec(2),
            titleTrailingLayout = {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(randomColor),
                )
            },
            actions = listOf(
                object : SettingsAction {
                    @Composable
                    override fun Composable() {
                        OSSwitchRow(checked = true, onCheckedChange = { }, label = loremIpsumSpec(2))
                        OSSwitchRow(checked = false, onCheckedChange = { }, label = loremIpsumSpec(3))
                    }
                },
            ),
            footer = loremIpsumSpec(10),
        )
    }
}
