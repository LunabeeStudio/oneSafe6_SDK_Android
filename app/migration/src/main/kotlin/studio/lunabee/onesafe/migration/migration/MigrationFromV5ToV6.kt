/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import studio.lunabee.onesafe.migration.MigrationSafeData0
import javax.inject.Inject

/**
 * • Remove ImportExport drive isDriveApiAuthorized preference key (useless)
 * • Launch new auto backup worker if auto backup enabled. The old one will be removed because of [ClassNotFoundException]
 */
class MigrationFromV5ToV6 @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
) : AppMigration0(5, 6) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> = OSError.runCatching {
        val safeId = migrationSafeData.id
        dataStore.edit {
            it.remove(booleanPreferencesKey("1d361eb7-a13f-49d1-9f9b-a37520c12361"))
        }
        if (getAutoBackupModeUseCase(safeId) != AutoBackupMode.Disabled) {
            autoBackupWorkersHelper.start(false, safeId)
        }
    }
}
