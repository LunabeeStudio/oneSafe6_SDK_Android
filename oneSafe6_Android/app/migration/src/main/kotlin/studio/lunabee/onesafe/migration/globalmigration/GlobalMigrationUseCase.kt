/*
 * Copyright (c) 2025-2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 3/7/2025 - for the oneSafe6 SDK.
 * Last modified 3/7/25, 12:51 PM
 */

package studio.lunabee.onesafe.migration.globalmigration

import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.repository.GlobalSettingsRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository

private val logger = LBLogger.get<GlobalMigrationUseCase>()

class GlobalMigrationUseCase @Inject constructor(
    @BuildNumber private val currentVersion: Int,
    private val globalSettingsRepository: GlobalSettingsRepository,
    private val autoBackupSettingsRepository: dagger.Lazy<AutoBackupSettingsRepository>,
    private val globalMigration1: dagger.Lazy<GlobalMigration1>,
) {
    suspend operator fun invoke() {
        val lastVersion = globalSettingsRepository.getAppVersion()
        logger.v("Found last version $lastVersion (current $currentVersion)")
        if (lastVersion == null) {
            val safeAutoBackupEnabled = autoBackupSettingsRepository.get().getSafeAutoBackupEnabled()
            val isFirstMigration = safeAutoBackupEnabled.isNotEmpty()
            if (isFirstMigration) {
                globalMigration1.get().migrate(safeAutoBackupEnabled)
            }
        }
        // Further migrations here if needed

        globalSettingsRepository.setAppVersion(currentVersion)
    }
}
