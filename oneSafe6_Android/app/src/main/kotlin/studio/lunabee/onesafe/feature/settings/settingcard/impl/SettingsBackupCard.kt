package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.dialog.FeatureComingDialogState
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionExportData
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionImportData
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionSynchronizeOption
import studio.lunabee.onesafe.importexport.settings.CardSettingsActionAutoBackupOption
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingsBackupCard(
    modifier: Modifier = Modifier,
    onClickOnImport: () -> Unit,
    onClickOnExport: () -> Unit,
    onClickOnAutoBackup: () -> Unit,
) {
    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_backupCard_title),
        actions = listOf(
            CardSettingsActionAutoBackupOption(onClickOnAutoBackup),
            CardSettingsActionImportData(onClickOnImport),
            CardSettingsActionExportData(onClickOnExport),
            CardSettingsActionSynchronizeOption { dialogState = FeatureComingDialogState { dialogState = null } },
        ),
        modifier = modifier,
    )
}

@OsDefaultPreview
@Composable
fun SettingBackupCardPreview() {
    OSTheme {
        SettingsBackupCard(
            onClickOnImport = {},
            onClickOnExport = {},
            onClickOnAutoBackup = {},
        )
    }
}
