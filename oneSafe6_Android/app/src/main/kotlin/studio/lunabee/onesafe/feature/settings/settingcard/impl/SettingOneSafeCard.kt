package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionAbout
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionRateUs
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingOneSafeCard(
    modifier: Modifier = Modifier,
    onClickOnAbout: () -> Unit,
    onClickOnRateUs: () -> Unit,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.application_name),
        actions = listOf(
            CardSettingsActionRateUs(onClickOnRateUs),
            CardSettingsActionAbout(onClickOnAbout),
        ),
        modifier = modifier,
    )
}

@OsDefaultPreview
@Composable
fun SettingOneSafeCardPreview() {
    OSPreviewOnSurfaceTheme {
        SettingOneSafeCard(
            onClickOnAbout = {},
            onClickOnRateUs = {},
        )
    }
}
