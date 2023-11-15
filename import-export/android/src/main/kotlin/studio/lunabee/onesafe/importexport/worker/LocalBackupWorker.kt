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

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.commonui.utils.setForegroundSafe
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.usecase.DeleteOldLocalBackupsUseCase
import studio.lunabee.onesafe.importexport.usecase.LocalAutoBackupUseCase
import studio.lunabee.onesafe.importexport.utils.ForegroundInfoCompat
import timber.log.Timber

/**
 * Worker used to schedule and run [LocalAutoBackupUseCase] with a foreground notification
 */
@HiltWorker
class LocalBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val localAutoBackupUseCase: LocalAutoBackupUseCase,
    private val deleteOldBackupsUseCase: DeleteOldLocalBackupsUseCase,
    private val osNotificationManager: OSNotificationManager,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var workerResult = Result.success()

        localAutoBackupUseCase()
            .onStart {
                updateProgress(0f)
            }
            .collect { result ->
                when (result) {
                    is LBFlowResult.Failure -> {
                        val data = Data.Builder()
                            .putString(ImportExportAndroidConstants.ERROR_OUTPUT_KEY, result.throwable.toString())
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
        private const val PROGRESS_DATA_KEY: String = "375f2850-9884-4ef7-a50b-6e58be73a483"

        fun getWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<LocalBackupWorker>()
                .addTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }

        fun start(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    ImportExportAndroidConstants.AUTO_BACKUP_WORKER_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    getWorkRequest(),
                )
        }
    }
}
