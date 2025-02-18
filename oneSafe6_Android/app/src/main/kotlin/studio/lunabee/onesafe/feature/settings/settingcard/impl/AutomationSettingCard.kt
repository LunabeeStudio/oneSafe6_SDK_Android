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
fun AutomationSettingCard(
    isAutomationEnabled: Boolean,
    toggleAutomation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_personalization_section_automationTitle),
        modifier = modifier,
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.settings_personalization_section_automation_fetchIconTitle),
                description = LbcTextSpec.StringResource(OSString.settings_personalization_section_automation_fetchIconLabel),
                onValueChange = { toggleAutomation() },
                isChecked = isAutomationEnabled,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun AutomationSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        AutomationSettingCard(
            isAutomationEnabled = true,
            toggleAutomation = {},
        )
    }
}
