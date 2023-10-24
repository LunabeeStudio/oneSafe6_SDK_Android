/*
 * Copyright (c) 2023-2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 10/17/2023 - for the oneSafe6 SDK.
 * Last modified 10/9/23, 6:29 PM
 */

package studio.lunabee.onesafe.importexport.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoBackupSettingsMainCard(
    toggleAutoBackup: () -> Unit,
    uiState: AutoBackupSettingsMainCardUiState,
    modifier: Modifier = Modifier,
) {
    Column {
        SettingsCard(
            title = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_settings_title),
            modifier = modifier,
            actions = listOfNotNull(
                SwitchSettingAction(
                    label = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_allowAutoBackup_title),
                    onValueChange = { toggleAutoBackup() },
                    isChecked = uiState is AutoBackupSettingsMainCardUiState.Enabled,
                ),
                (uiState as? AutoBackupSettingsMainCardUiState.Enabled)?.let { uiState ->
                    CardSettingsSelectFrequency(
                        frequency = uiState.autoBackupFrequency,
                        onClick = uiState.selectAutoBackupFrequency,
                    )
                },
            ),
        )
        OSSmallSpacer()
        OSText(
            text = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_autoBackup_footer, ImportExportConstant.KeepBackupsNumber),
            style = MaterialTheme.typography.bodySmall,
            color = LocalColorPalette.current.Neutral60,
        )
    }
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsMainCardPreview() {
    OSTheme {
        AutoBackupSettingsMainCard(
            toggleAutoBackup = {},
            uiState = AutoBackupSettingsMainCardUiState.Enabled(
                selectAutoBackupFrequency = { },
                autoBackupFrequency = AutoBackupFrequency.WEEKLY,
            ),
        )
    }
}
