package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ShakeToLockSettingCard(
    shakeToLockEnabled: Boolean,
    toggleShakeToLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_shake_title),
        modifier = modifier,
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.settings_security_section_shake_lock),
                onValueChange = { toggleShakeToLock() },
                isChecked = shakeToLockEnabled,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun ShakeToLockSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        ClipboardClearSettingCard(ClipboardClearDelay.THIRTY_SECONDS, {})
    }
}
