/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 31/08/2023 15:18
 */

package studio.lunabee.onesafe.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import javax.inject.Inject

/**
 * • Remove ImportExport drive isDriveApiAuthorized preference key (useless)
 * • Launch new auto backup worker if auto backup enabled. The old one will be removed because of [ClassNotFoundException]
 */
class MigrationFromV5ToV6 @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        dataStore.edit {
            it.remove(booleanPreferencesKey("1d361eb7-a13f-49d1-9f9b-a37520c12361"))
        }
        if (getAutoBackupModeUseCase() != AutoBackupMode.Disabled) {
            autoBackupWorkersHelper.start(false)
        }
    }
}
