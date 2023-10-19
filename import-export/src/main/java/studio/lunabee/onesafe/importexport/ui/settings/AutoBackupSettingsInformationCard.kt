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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 1:14 PM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.importexport.utils.BackupDateTimeLocaleFormatter
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant
import java.util.Locale

@Composable
fun AutoBackupSettingsInformationCard(
    date: Instant?,
    modifier: Modifier = Modifier,
) {
    val dateText = if (date == null) {
        LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_informations_noBackups)
    } else {
        val context = LocalContext.current
        val locale = Locale(context.getString(R.string.locale_lang))
        LbcTextSpec.Raw(BackupDateTimeLocaleFormatter(locale).format(date))
    }

    OSCustomCard(
        modifier = modifier,
        title = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_informations_title),
        content = {
            OSRow(
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                label = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_lastAutoBackupDate_title),
                text = dateText,
            )
        },
    )
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsInformationCardPreview() {
    OSTheme {
        AutoBackupSettingsInformationCard(
            Instant.now(),
        )
    }
}
