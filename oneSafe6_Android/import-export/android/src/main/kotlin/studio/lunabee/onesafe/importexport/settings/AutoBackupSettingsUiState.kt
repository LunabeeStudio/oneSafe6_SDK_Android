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

import android.content.Intent
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.importexport.settings.backupnumber.AutoBackupMaxNumber
import studio.lunabee.onesafe.model.OSSwitchState
import java.net.URI

data class AutoBackupSettingsUiState(
    val isAutoBackupEnabled: Boolean,
    val autoBackupFrequency: AutoBackupFrequency,
    val autoBackupMaxNumber: AutoBackupMaxNumber,
    val latestBackups: LatestBackups?,
    val cloudBackupEnabledState: OSSwitchState,
    val isKeepLocalBackupEnabled: Boolean,
    val toggleKeepLocalBackup: () -> Unit,
    val driveUri: URI?,
    val driveAccount: String?,
) {
    companion object {
        fun disabled(): AutoBackupSettingsUiState = AutoBackupSettingsUiState(
            isAutoBackupEnabled = false,
            autoBackupFrequency = AutoBackupFrequency.DAILY,
            autoBackupMaxNumber = AutoBackupMaxNumber.FIVE,
            latestBackups = null,
            cloudBackupEnabledState = OSSwitchState.False,
            isKeepLocalBackupEnabled = false,
            toggleKeepLocalBackup = {},
            driveUri = null,
            driveAccount = null,
        )
    }
}

data class AutoBackupSettingsDriveAuth(
    val authorizeIntent: Intent,
    val onAuthorize: (Boolean) -> Unit,
)
