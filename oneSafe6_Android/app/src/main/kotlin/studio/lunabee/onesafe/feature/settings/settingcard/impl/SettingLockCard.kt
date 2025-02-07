package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.CardSettingsButtonAction
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingLockCard(
    modifier: Modifier = Modifier,
    onSafeDeletion: () -> Unit,
) {
    val osDesignSystem = LocalDesignSystem.current

    val actions = listOf(
        CardSettingsButtonAction(
            onClick = onSafeDeletion,
            icon = OSDrawable.ic_delete,
            text = LbcTextSpec.StringResource(OSString.settings_multiSafe_deleteSafe),
            isDangerous = true,
        ),
    )

    SettingsCard(
        modifier = modifier,
        actions = actions.mapIndexed { idx, action ->
            action.settingsAction(osDesignSystem.getRowClickablePaddingValuesDependingOnIndex(idx, actions.size))
        },
    )
}

@OsDefaultPreview
@Composable
private fun SettingLockCardPreview() {
    OSPreviewOnSurfaceTheme {
        SettingLockCard(
            onSafeDeletion = {},
        )
    }
}
