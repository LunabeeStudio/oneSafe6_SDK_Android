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

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.importexport.settings.backupnumber.AutoBackupMaxNumber
import studio.lunabee.onesafe.model.OSSwitchState

internal sealed class AutoBackupSettingsMainCardUiState {
    abstract val toggleAutoBackup: () -> Unit

    data class Disabled(
        override val toggleAutoBackup: () -> Unit,
        val footer: LbcTextSpec = LbcTextSpec
            .StringResource(OSString.settings_autoBackupMaxNumberScreen_withBubbles_description),
    ) : AutoBackupSettingsMainCardUiState()

    data class Enabled(
        override val toggleAutoBackup: () -> Unit,
        val selectAutoBackupFrequency: () -> Unit,
        val selectAutoBackupMaxNumber: () -> Unit,
        val autoBackupFrequency: AutoBackupFrequency,
        val autoBackupMaxNumber: AutoBackupMaxNumber,
        val isCloudBackupEnabled: OSSwitchState,
        val isKeepLocalBackupEnabled: Boolean,
        val toggleKeepLocalBackup: () -> Unit,
        val toggleCloudBackup: (() -> Unit)?,
    ) : AutoBackupSettingsMainCardUiState()
}
