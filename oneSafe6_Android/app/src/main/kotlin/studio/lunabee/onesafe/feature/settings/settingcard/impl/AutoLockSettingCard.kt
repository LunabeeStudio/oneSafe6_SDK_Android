package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionAutoLockAppChangeAction
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionAutoLockInactivityAction
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun AutoLockSettingCard(
    appChangeDelay: AutoLockBackgroundDelay,
    inactivityDelay: AutoLockInactivityDelay,
    showAppChangeDelayBottomSheet: () -> Unit,
    showInactivityDelayBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_autolock_title),
        modifier = modifier,
        actions = listOf(
            CardSettingsActionAutoLockInactivityAction(
                delay = inactivityDelay,
                onClick = showInactivityDelayBottomSheet,
            ),
            CardSettingsActionAutoLockAppChangeAction(
                delay = appChangeDelay,
                onClick = showAppChangeDelayBottomSheet,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun AutoLockSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        AutoLockSettingCard(
            appChangeDelay = AutoLockBackgroundDelay.FIVE_MINUTES,
            inactivityDelay = AutoLockInactivityDelay.NEVER,
            showAppChangeDelayBottomSheet = {},
            showInactivityDelayBottomSheet = {},
        )
    }
}
