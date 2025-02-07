package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

@Composable
fun MaterialYouSettingCard(
    isMaterialYouEnabled: Boolean,
    toggleMaterialYou: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_personalization_section_materialYou_title),
        modifier = modifier,
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.settings_personalization_section_materialYou_label),
                onValueChange = { toggleMaterialYou() },
                isChecked = isMaterialYouEnabled,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun MaterialYouSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        MaterialYouSettingCard(
            isMaterialYouEnabled = false,
            toggleMaterialYou = {},
        )
    }
}
