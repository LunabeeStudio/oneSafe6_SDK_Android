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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.settings.AutoBackupFrequency
import studio.lunabee.onesafe.importexport.usecase.GetDurationBeforeBackupOutdatedUseCase
import studio.lunabee.onesafe.importexport.usecase.SynchronizeCloudBackupsUseCase
import studio.lunabee.onesafe.jvm.toByteArray
import java.time.Clock
import kotlin.time.Duration
import kotlin.time.toJavaDuration

// TODO <AutoBackup> unit test the schedule logic

private val logger = LBLogger.get<AutoBackupSchedulerWorker>()

/**
 * Schedule the [AutoBackupChainWorker] according to user settings. Also run a first cloud synchronization if needed (usually when the user
 * just switch on the cloud auto backup feature. Use a worker to make sure the [AutoBackupChainWorker] is scheduled even if the user quit
 * the app quickly after enabling backups (or app crash).
 */
@HiltWorker
class AutoBackupSchedulerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getDurationBeforeBackupOutdatedUseCase: GetDurationBeforeBackupOutdatedUseCase,
    private val settings: AutoBackupSettingsRepository,
    private val synchronizeCloudBackupsUseCase: SynchronizeCloudBackupsUseCase,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val safeId = inputData.getByteArray(EXPORT_WORKER_SAFE_ID_DATA)?.let { SafeId(it) }

        return if (safeId == null) {
            logger.e("Missing SafeId param")
            val data = Data.Builder()
                .putString(ERROR_OUTPUT_KEY, "Missing SafeId param")
                .build()
            Result.failure(data)
        } else if (safeRepository.getSafeVersion(safeId) == null) {
            logger.i("No safe found for the provided safeId. Remove workers")
            autoBackupWorkersHelper.cancel(safeId)
            Result.success()
        } else {
            val durationBeforeBackupOutdated = getDurationBeforeBackupOutdatedUseCase(safeId)
            val frequency = AutoBackupFrequency.valueForDuration(settings.autoBackupFrequency(safeId))
            val synchronizeCloudFirst = inputData.getBoolean(SYNCHRONIZE_CLOUD_FIRST_DATA, false)
            val islastBackupOutdated = durationBeforeBackupOutdated <= Duration.ZERO

            // Trigger a cloud sync if requested before scheduling the backup worker chain, except if we know that the chain worker will run
            // immediately (ie. last backup is outdated)
            if (!islastBackupOutdated && synchronizeCloudFirst && settings.cloudBackupEnabled(safeId).first()) {
                synchronizeCloudBackupsUseCase(safeId)
                    .onCompletion { error ->
                        // Ignore error, let the scheduled backup synchronize later
                        error?.let(logger::e)
                    }
                    .collect()
            }

            val workManager = WorkManager.getInstance(applicationContext)
            val inputData = Data.Builder()
                .putByteArray(EXPORT_WORKER_SAFE_ID_DATA, safeId.toByteArray())
                .build()
            val periodicBuilder = PeriodicWorkRequestBuilder<AutoBackupChainWorker>(
                repeatInterval = frequency.repeat.toJavaDuration(),
                flexTimeInterval = frequency.flex.toJavaDuration(),
            )
                .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(safeId))
                .setInputData(inputData)

            if (islastBackupOutdated) {
                val data = Data.Builder()
                    .putByteArray(AutoBackupChainWorker.AUTO_BACKUP_CHAIN_WORKER_SAFE_ID_DATA, safeId.id.toByteArray())
                    .build()
                // Trigger asap if the backup is outdated
                val workRequest = OneTimeWorkRequestBuilder<AutoBackupChainWorker>()
                    .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(safeId))
                    .setInputData(data)
                    .build()
                workManager
                    .enqueueUniqueWork(autoBackupInitialChainWorkName(safeId), ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)
            } else {
                // Try to set the next schedule to the exact computed time. Depending of the current state of the work request,
                // it might does nothing
                // TODO <AutoBackup> does not work as expected, trigger manually if durationBeforeBackupOutdated == 0 or if no backup
                //  at all yet
                periodicBuilder.setNextScheduleTimeOverride(clock.millis() + durationBeforeBackupOutdated.inWholeMilliseconds)
            }

            workManager
                .enqueueUniquePeriodicWork(
                    autoBackupChainWorkName(safeId),
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicBuilder.build(),
                ).await()

            Result.success()
        }
    }

    companion object {
        internal fun autoBackupChainWorkName(safeId: SafeId): String = "32183d09-7402-407f-b492-7be45f3a148c${safeId.id}"
        private fun autoBackupInitialChainWorkName(safeId: SafeId): String = "3163c4a1-5157-43dd-ada9-611114e0ca41_${safeId.id}"
        internal const val SYNCHRONIZE_CLOUD_FIRST_DATA: String = "373ad937-98c8-4b42-9dea-43df44985d00"
        internal const val EXPORT_WORKER_SAFE_ID_DATA = "bfa2d4da-ebfe-4231-87ee-29b012109f7b"
        private const val ERROR_OUTPUT_KEY: String = "594b6598-9871-443e-ba1e-1ea408da49e2"
    }
}
