package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.CardSettingsNavAction
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionChangePasswordOption
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionPersonalizationOption
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionSecurityOption
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingSecurityCard(
    modifier: Modifier = Modifier,
    onClickSecurityOption: () -> Unit,
    onClickOnPersonalizationOption: () -> Unit,
    onClickChangePasswordOption: () -> Unit,
) {
    val actions = mutableListOf<CardSettingsNavAction>()

    actions.add(CardSettingsActionSecurityOption(onClickSecurityOption))
    actions.add(CardSettingsActionChangePasswordOption(onClickChangePasswordOption))
    actions.add(CardSettingsActionPersonalizationOption(onClickOnPersonalizationOption))

    if (actions.isNotEmpty()) {
        SettingsCard(
            title = LbcTextSpec.StringResource(OSString.settings_section_security_title),
            actions = actions,
            modifier = modifier,
        )
    }
}

@OsDefaultPreview
@Composable
fun SettingSecurityCardPreview() {
    OSPreviewOnSurfaceTheme {
        SettingSecurityCard(
            onClickSecurityOption = {},
            onClickChangePasswordOption = {},
            onClickOnPersonalizationOption = {},
        )
    }
}
