/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Last modified 3/7/25, 12:04 PM
 */

package studio.lunabee.onesafe.migration.globalmigration

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.SafeAutoBackupEnabled
import studio.lunabee.onesafe.importexport.worker.AutoBackupSchedulerWorker

/**
 * Cancel all backup workers and re-schedules them according to user settings
 */
class GlobalMigration1 @Inject constructor(
    private val workManager: WorkManager,
) {
    fun migrate(safeAutoBackupEnabled: List<SafeAutoBackupEnabled>) {
        safeAutoBackupEnabled.forEach { backupEnabled ->
            workManager.cancelAllWorkByTag("fac62754-ddfe-4777-aeb6-e59591bbfc5c_${backupEnabled.safeId.id}")

            val data = Data
                .Builder()
                .putBoolean("373ad937-98c8-4b42-9dea-43df44985d00", backupEnabled.cloudAutoBackupEnabled)
                .putByteArray("bfa2d4da-ebfe-4231-87ee-29b012109f7b", backupEnabled.safeId.toByteArray())
                .build()

            val workRequest = OneTimeWorkRequestBuilder<AutoBackupSchedulerWorker>()
                .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(backupEnabled.safeId))
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName = "1d35209a-c713-439b-80a1-f83791018682",
                existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
                request = workRequest,
            )
        }
    }
}
