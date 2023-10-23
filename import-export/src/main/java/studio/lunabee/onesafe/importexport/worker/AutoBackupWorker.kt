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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 12:05 PM
 */

package studio.lunabee.onesafe.importexport.worker

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.commonui.utils.setForegroundSafe
import studio.lunabee.onesafe.importexport.ui.settings.AutoBackupFrequency
import studio.lunabee.onesafe.importexport.usecase.AutoBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.DeleteOldLocalBackupsUseCase
import studio.lunabee.onesafe.importexport.utils.ForegroundInfoCompat
import timber.log.Timber
import kotlin.time.toJavaDuration

/**
 * Worker used to schedule and run [AutoBackupUseCase] with a foreground notification
 */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val autoBackupUseCase: AutoBackupUseCase,
    private val deleteOldBackupsUseCase: DeleteOldLocalBackupsUseCase,
    private val osNotificationManager: OSNotificationManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var workerResult = Result.success()

        autoBackupUseCase()
            .onStart {
                updateProgress(0f)
            }
            .collect { result ->
                when (result) {
                    is LBFlowResult.Failure -> {
                        val data = Data.Builder()
                            .putString(ERROR_OUTPUT_KEY, result.throwable.toString())
                            .build()
                        Timber.e(result.throwable)
                        workerResult = Result.failure(data)
                    }
                    is LBFlowResult.Loading -> updateProgress(result.progress ?: -1f)
                    is LBFlowResult.Success -> {
                        deleteOldBackupsUseCase()
                        workerResult = Result.success()
                    }
                }
            }

        return workerResult
    }

    private suspend fun updateProgress(progress: Float) {
        getForegroundInfo()
        val data = Data.Builder()
            .putFloat(PROGRESS_DATA_KEY, progress)
            .build()
        setProgress(data)
        val foregroundInfo = createForegroundInfo(progress)
        setForegroundSafe(foregroundInfo)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(0f)

    private fun createForegroundInfo(progress: Float): ForegroundInfo {
        Timber.d("Progress $progress") // TODO show progress

        val title = applicationContext.getString(R.string.notification_autobackup_progress_title)
        val notification = osNotificationManager.backupNotificationBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        return ForegroundInfoCompat.foregroundInfoDataSync(
            notificationId = OSNotificationManager.AUTO_BACKUP_WORKER_NOTIFICATION_ID,
            notification = notification,
        )
    }

    companion object {
        private const val AUTO_BACKUP_WORK_NAME = "00ff9185-1269-497b-88a6-582b422d5f7b"
        private const val AUTO_BACKUP_PERIODIC_WORK_NAME = "1fee3b5c-6c52-45cf-8966-8508350d9cc8"

        // TODO share tag with future drive worker for cancellation
        private const val AUTO_BACKUP_WORKER_TAG = "fac62754-ddfe-4777-aeb6-e59591bbfc5c"

        private const val ERROR_OUTPUT_KEY = "ab2c1e17-2b69-4839-b954-bf2b8a3fab73"
        private const val PROGRESS_DATA_KEY = "375f2850-9884-4ef7-a50b-6e58be73a483"

        fun start(
            context: Context,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(AUTO_BACKUP_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)
        }

        fun schedule(
            context: Context,
            frequency: AutoBackupFrequency,
        ) {
            val workRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                repeatInterval = frequency.repeat.toJavaDuration(),
                flexTimeInterval = frequency.flex.toJavaDuration(),
            )
                .addTag(AUTO_BACKUP_WORKER_TAG)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(AUTO_BACKUP_PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, workRequest)
        }

        fun cancel(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag(AUTO_BACKUP_WORKER_TAG)
        }
    }
}
