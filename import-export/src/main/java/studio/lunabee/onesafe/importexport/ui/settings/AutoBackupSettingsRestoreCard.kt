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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun AutoBackupSettingsRestoreCard(
    onRestoreBackupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_restore_title),
        modifier = modifier,
        actions = listOf(
            CardSettingsRestoreAutoBackup(onRestoreBackupClick),
        ),
    )
}

@OsDefaultPreview
@Composable
fun AutoBackupSettingsRestoreCardPreview() {
    OSTheme {
        AutoBackupSettingsRestoreCard(
            onRestoreBackupClick = {},
        )
    }
}