package studio.lunabee.onesafe.feature.settings.bubbles

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.settings.bubbles.model.BubblesResendMessageDelay
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionResendBubblesMessage

@Composable
fun ExtensionBubblesCard(
    modifier: Modifier = Modifier,
    isBubblesPreviewActivated: Boolean,
    toggleBubblesPreview: (Boolean) -> Unit,
    onResendMessageClick: () -> Unit,
    bubblesResendMessageDelay: BubblesResendMessageDelay,
) {
    SettingsCard(
        modifier = modifier,
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.bubblesSettings_displayPreview),
                onValueChange = toggleBubblesPreview,
                isChecked = isBubblesPreviewActivated,
            ),
            CardSettingsActionResendBubblesMessage(
                onClick = onResendMessageClick,
                secondaryText = bubblesResendMessageDelay.text,
            ),
        ),
    )
}
