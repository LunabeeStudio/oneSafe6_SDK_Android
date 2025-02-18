package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionVerifyPasswordOption
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

@Composable
fun VerifyPasswordSettingCard(
    modifier: Modifier = Modifier,
    interval: VerifyPasswordInterval,
    openIntervalBottomSheet: () -> Unit,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_verifyPassword_title),
        footer = LbcTextSpec.StringResource(OSString.settings_security_section_verifyPassword_footer),
        modifier = modifier,
        actions = listOf(
            CardSettingsActionVerifyPasswordOption(
                interval = interval,
                onClick = openIntervalBottomSheet,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun VerifyPasswordSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        VerifyPasswordSettingCard(
            interval = VerifyPasswordInterval.EVERY_MONTH,
            openIntervalBottomSheet = {},
        )
    }
}
