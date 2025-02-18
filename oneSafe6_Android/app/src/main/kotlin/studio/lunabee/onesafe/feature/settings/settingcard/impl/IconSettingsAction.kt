package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsAction
import studio.lunabee.onesafe.feature.settings.personalization.AppIconUi
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

class IconSettingsAction(
    private val currentAliasSelected: AppIcon,
    private val onIconAliasClick: (iconAlias: AppIcon) -> Unit,
) : SettingsAction {
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Composable() {
        Column {
            OSText(
                text = LbcTextSpec.StringResource(OSString.settings_personalization_iconAndName_subtitle),
                modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
                style = MaterialTheme.typography.bodyLarge,
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppIcon.entries.forEach { iconAlias ->
                    val iconUi = AppIconUi.fromAppIcon(iconAlias)
                    val modifier = if (currentAliasSelected == iconAlias) {
                        Modifier // already selected icon is not clickable
                    } else {
                        Modifier
                            .clickable(onClick = { onIconAliasClick(iconAlias) })
                    }
                    iconUi.Composable(
                        isSelected = currentAliasSelected == iconAlias,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun IconSettingsActionPreview() {
    OSPreviewOnSurfaceTheme {
        IconSettingsAction(
            currentAliasSelected = AppIcon.Default,
            onIconAliasClick = {},
        ).Composable()
    }
}
