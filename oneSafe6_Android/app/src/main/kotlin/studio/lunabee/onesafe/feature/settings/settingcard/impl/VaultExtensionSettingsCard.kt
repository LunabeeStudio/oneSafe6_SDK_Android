package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionBubblesOption

@Composable
fun VaultExtensionSettingsCard(
    navigateToBubblesSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_section_vault_extensions),
        modifier = modifier,
        actions = listOf(CardSettingsActionBubblesOption(onClick = navigateToBubblesSettings)),
    )
}
