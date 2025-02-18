package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionClipboardCleanAction
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

@Composable
fun ClipboardClearSettingCard(
    delay: ClipboardClearDelay,
    showClipboardClearBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_security_section_clipboard_title),
        modifier = modifier,
        actions = listOf(
            CardSettingsActionClipboardCleanAction(
                delay = delay,
                onClick = showClipboardClearBottomSheet,
            ),
        ),
    )
}

@OsDefaultPreview
@Composable
fun ClipboardClearSettingCardPreview() {
    OSPreviewOnSurfaceTheme {
        ClipboardClearSettingCard(ClipboardClearDelay.THIRTY_SECONDS, {})
    }
}
