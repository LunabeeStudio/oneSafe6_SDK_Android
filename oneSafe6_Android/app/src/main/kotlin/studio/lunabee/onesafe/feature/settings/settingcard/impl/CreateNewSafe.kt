package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionCreateNewSafeAction
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingsCreateNewSafeCard(
    modifier: Modifier = Modifier,
    createNewSafe: () -> Unit,
) {
    val action = listOf(
        CardSettingsActionCreateNewSafeAction(createNewSafe),
    )
    SettingsCard(
        actions = action,
        modifier = modifier,
    )
}

@OsDefaultPreview
@Composable
private fun SettingsCreateNewSafeCardPreview() {
    OSTheme {
        SettingsCreateNewSafeCard(
            createNewSafe = {},
        )
    }
}
