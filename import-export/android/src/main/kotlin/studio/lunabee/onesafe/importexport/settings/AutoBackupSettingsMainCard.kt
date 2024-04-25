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
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.importexport.settings.backupnumber.AutoBackupMaxNumber
import studio.lunabee.onesafe.model.OSSwitchState
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoBackupSettingsMainCard(
    uiState: AutoBackupSettingsMainCardUiState,
    featureFlagCloudBackup: Boolean,
    modifier: Modifier = Modifier,
) {
    Column {
        SettingsCard(
            title = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_settings_title),
            modifier = modifier,
            actions = buildList {
                this += SwitchSettingAction(
                    label = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_allowAutoBackup_title),
                    onValueChange = { uiState.toggleAutoBackup() },
                    isChecked = uiState is AutoBackupSettingsMainCardUiState.Enabled,
                )
                (uiState as? AutoBackupSettingsMainCardUiState.Enabled)?.let { uiState ->
                    if (featureFlagCloudBackup) {
                        uiState.toggleCloudBackup?.let { toggleCloudBackup ->
                            this += SwitchSettingAction(
                                label = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_allowAutoBackupOnGoogleDrive_title),
                                onValueChange = { toggleCloudBackup() },
                                isChecked = uiState.isCloudBackupEnabled,
                            )
                        }
                        if (uiState.isCloudBackupEnabled.checked) {
                            this += SwitchSettingAction(
                                label = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_keepAutoBackupOnLocal_title),
                                description = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_keepAutoBackupOnLocal_subtitle),
                                onValueChange = { uiState.toggleKeepLocalBackup() },
                                isChecked = uiState.isKeepLocalBackupEnabled,
                            )
                        }
                    }
                    this += CardSettingsSelectFrequency(
                        frequency = uiState.autoBackupFrequency,
                        onClick = uiState.selectAutoBackupFrequency,
                    )
                    this += CardSettingsSelectBackupNumber(
                        number = uiState.autoBackupMaxNumber,
                        onClick = uiState.selectAutoBackupMaxNumber,
                    )
                }
            },
        )
        OSSmallSpacer()
        if (uiState is AutoBackupSettingsMainCardUiState.Disabled) {
            OSText(
                text = uiState.footer,
                style = MaterialTheme.typography.bodySmall,
                color = LocalColorPalette.current.Neutral60,
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsMainCardPreview() {
    OSTheme {
        AutoBackupSettingsMainCard(
            uiState = AutoBackupSettingsMainCardUiState.Enabled(
                selectAutoBackupFrequency = { },
                selectAutoBackupMaxNumber = { },
                autoBackupFrequency = AutoBackupFrequency.WEEKLY,
                autoBackupMaxNumber = AutoBackupMaxNumber.FIVE,
                isCloudBackupEnabled = OSSwitchState.True,
                isKeepLocalBackupEnabled = false,
                toggleAutoBackup = {},
                toggleCloudBackup = {},
                toggleKeepLocalBackup = {},
            ),
            featureFlagCloudBackup = true,
        )
    }
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsMainCardDisabledPreview() {
    OSTheme {
        AutoBackupSettingsMainCard(
            uiState = AutoBackupSettingsMainCardUiState.Disabled(
                toggleAutoBackup = {},
            ),
            featureFlagCloudBackup = true,
        )
    }
}
