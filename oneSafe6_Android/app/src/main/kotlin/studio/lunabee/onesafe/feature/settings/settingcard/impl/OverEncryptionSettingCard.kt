package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionOverEncryptionOption
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OverEncryptionSettingCard(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onClickOnStatus: () -> Unit,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_overEncryption_title),
        modifier = modifier,
        actions = listOf(
            CardSettingsActionOverEncryptionOption(
                onClick = onClickOnStatus,
                isEnabled = isEnabled,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun OverEncryptionSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        CardSettingsActionOverEncryptionOption(
            onClick = {},
            isEnabled = true,
        )
    }
}
