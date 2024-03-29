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
 * Last modified 10/17/23, 3:41 PM
 */

package studio.lunabee.onesafe.importexport.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.utils.BackupDateTimeLocaleFormatter
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.io.File
import java.time.Instant
import java.util.Locale

@Composable
fun AutoBackupSettingsInformationCard(
    latestBackups: LatestBackups?,
    modifier: Modifier = Modifier,
) {
    OSCustomCard(
        modifier = modifier,
        title = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_informations_title),
        content = {
            if (latestBackups?.latest == null) {
                OSRow(
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    label = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_lastAutoBackupDate_title),
                    text = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_informations_noBackups),
                )
            } else {
                val context = LocalContext.current
                val locale = Locale(context.getString(OSString.locale_lang))
                val backupCount = latestBackups.count()
                Column(verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small)) {
                    latestBackups.local?.date?.let { date ->
                        val label = if (backupCount == 1) {
                            LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_lastAutoBackupDate_title)
                        } else {
                            LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_lastLocalAutoBackupDate_title)
                        }
                        OSRow(
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                            label = label,
                            text = LbcTextSpec.Raw(BackupDateTimeLocaleFormatter(locale).format(date)),
                        )
                        if (backupCount > 1) {
                            OSSmallSpacer()
                        }
                    }
                    latestBackups.cloud?.date?.let { date ->
                        val label = if (backupCount == 1) {
                            LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_lastAutoBackupDate_title)
                        } else {
                            LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_lastCloudAutoBackupDate_title)
                        }
                        OSRow(
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                            label = label,
                            text = LbcTextSpec.Raw(BackupDateTimeLocaleFormatter(locale).format(date)),
                        )
                    }
                }
            }
        },
    )
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsInformationCardPreview() {
    OSPreviewBackgroundTheme {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            AutoBackupSettingsInformationCard(
                LatestBackups(
                    LocalBackup(date = Instant.now(), file = File("")),
                    CloudBackup(remoteId = "", name = "", date = Instant.now()),
                ),
            )
            AutoBackupSettingsInformationCard(
                LatestBackups(
                    LocalBackup(date = Instant.now(), file = File("")),
                    null,
                ),
            )
            AutoBackupSettingsInformationCard(
                null,
            )
        }
    }
}

private fun LatestBackups.count(): Int {
    return when {
        local != null && cloud != null -> 2
        local != null || cloud != null -> 1
        else -> 0
    }
}
