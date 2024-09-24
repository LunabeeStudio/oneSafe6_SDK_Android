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
 * Last modified 10/17/23, 4:34 PM
 */

package studio.lunabee.onesafe.importexport.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lblogger.LBLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase

// TODO <AutoBackup> test (manually, maybe unit test?)

private val logger = LBLogger.get<AutoBackupChainWorker>()

/**
 * Launch the auto backup worker depending on user settings [AutoBackupMode]
 */
@HiltWorker
class AutoBackupChainWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val itemRepository: SafeItemRepository,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    private val featureFlags: FeatureFlags,
    private val workManager: WorkManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val safeId = inputData.getByteArray(AUTO_BACKUP_CHAIN_WORKER_SAFE_ID_DATA)?.let { SafeId(it) }

        return if (safeId == null) {
            logger.e("Missing SafeId param")
            val data = Data.Builder()
                .putString(AUTO_BACKUP_CHAIN_WORKER_SAFE_ID_ERROR_DATA, "Missing SafeId param")
                .build()
            Result.failure(data)
        } else {
            var backupMode: AutoBackupMode? = null
            try {
                backupMode = getAutoBackupModeUseCase(safeId)
                if (itemRepository.getSafeItemsCount(safeId) != 0) {
                    when (backupMode) {
                        AutoBackupMode.Disabled -> {
                            // Unexpected, log and cancel workers
                            logger.e(
                                "${AutoBackupChainWorker::class.simpleName} run but ${AutoBackupMode::class.simpleName} is $backupMode",
                            )
                            autoBackupWorkersHelper.cancel()
                        }
                        AutoBackupMode.LocalOnly -> LocalBackupWorker.start(
                            workManager,
                            featureFlags.backupWorkerExpedited(),
                            safeId,
                        )
                        AutoBackupMode.CloudOnly -> CloudBackupWorker.start(
                            workManager,
                            featureFlags.backupWorkerExpedited(),
                            safeId,
                        )
                        AutoBackupMode.Synchronized -> {
                            val localWorkRequest = LocalBackupWorker.getWorkRequest(featureFlags.backupWorkerExpedited(), safeId)
                            val cloudSyncWorkRequest = CloudSynchronizeBackupWorker.getWorkRequest(safeId)

                            WorkManager.getInstance(applicationContext)
                                .beginUniqueWork(
                                    autoBackupUniqueChainWorkName(safeId),
                                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                                    localWorkRequest,
                                )
                                .then(cloudSyncWorkRequest)
                                .enqueue()
                        }
                    }
                }
            } catch (e: Throwable) {
                return autoBackupWorkersHelper.onBackupWorkerFails(
                    error = e,
                    runAttemptCount = runAttemptCount,
                    errorSource = backupMode ?: AutoBackupMode.Disabled,
                    safeId = safeId,
                )
            }

            Result.success()
        }
    }

    companion object {
        fun autoBackupUniqueChainWorkName(safeId: SafeId): String = "d725ac85-8742-46db-930d-7903470d05a6_$safeId"
        const val AUTO_BACKUP_CHAIN_WORKER_SAFE_ID_DATA: String = "7f9b8871-ba76-4563-91d9-7a5aacd169e9"
        internal const val AUTO_BACKUP_CHAIN_WORKER_SAFE_ID_ERROR_DATA = "14e547b1-1e07-42c8-b8cf-dc9aac5c30d6"
    }
}
