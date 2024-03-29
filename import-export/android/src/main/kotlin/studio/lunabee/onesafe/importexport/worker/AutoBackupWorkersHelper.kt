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
 * Created by Lunabee Studio / Date - 11/20/2023 - for the oneSafe6 SDK.
 * Last modified 11/20/23, 7:05 PM
 */

package studio.lunabee.onesafe.importexport.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.error.codeText
import studio.lunabee.onesafe.commonui.notification.OSNotificationChannelId
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

private val logger = LBLogger.get<AutoBackupWorkersHelper>()

class AutoBackupWorkersHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val osNotificationManager: OSNotificationManager,
    private val autoBackupErrorRepository: AutoBackupErrorRepository,
    private val clock: Clock,
) {
    fun start(
        synchronizeCloudFirst: Boolean,
    ) {
        val data = Data.Builder()
            .putBoolean(AutoBackupSchedulerWorker.SYNCHRONIZE_CLOUD_FIRST_DATA, synchronizeCloudFirst)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AutoBackupSchedulerWorker>()
            .addTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(AUTO_BACKUP_SCHEDULER_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)
    }

    fun cancel() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
    }

    private suspend fun isScheduled(): Boolean {
        val workManager = WorkManager.getInstance(context)
        return workManager
            .getWorkInfosForUniqueWorkFlow(AutoBackupSchedulerWorker.AUTO_BACKUP_CHAIN_WORK_NAME)
            .first()
            .firstOrNull()
            ?.state in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)
    }

    suspend fun ensureAutoBackupScheduled() {
        if (autoBackupSettingsRepository.autoBackupEnabled.first() && !isScheduled()) {
            logger.w("Re-scheduled auto backup worker")
            start(autoBackupSettingsRepository.cloudBackupEnabled.first())
        }
    }

    /**
     * Handle backup worker error
     *  • set backup error and send notification if [runAttemptCount] reaches [RETRIES_BEFORE_SHOW_ERROR]
     *  • compute worker result depending on the [error] and [runAttemptCount]
     *
     * @param error Error caught in backup worker
     * @param runAttemptCount see ListenableWorker.getRunAttemptCount
     * @return The worker [Result]
     */
    @SuppressLint("MissingPermission")
    suspend fun onBackupWorkerFails(error: Throwable?, runAttemptCount: Int): Result {
        logger.e("fail #$runAttemptCount", error)
        val canRetry = canRetry(error)

        if (runAttemptCount == RETRIES_BEFORE_SHOW_ERROR || !canRetry) {
            // Store error
            val autoBackupError = AutoBackupError(
                date = ZonedDateTime.now(clock),
                code = error.codeText().string(context),
                message = error?.stackTraceToString(),
            )
            autoBackupErrorRepository.setError(autoBackupError)
            // Notify
            if (osNotificationManager.areNotificationsEnabled(OSNotificationChannelId.BACKUP_CHANNEL_ID)) {
                val title = context.getString(OSString.notification_autobackup_error_title)
                val message = context.getString(OSString.notification_autobackup_error_message, error.codeText().string(context))
                val notificationBuilder = osNotificationManager.backupNotificationBuilder
                    .setContentTitle(title)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(message),
                    )
                    .setTicker(title)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        launchIntent,
                        PendingIntent.FLAG_IMMUTABLE,
                    )
                    notificationBuilder.setContentIntent(pendingIntent)
                }

                osNotificationManager.manager.notify(
                    OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID,
                    notificationBuilder.build(),
                )
            }
        }

        return if (canRetry) {
            Result.retry()
        } else {
            Result.failure()
        }
    }

    private fun canRetry(error: Throwable?): Boolean {
        val driveError = error as? OSDriveError
        val canRetry = when (driveError?.code) {
            OSDriveError.Code.REQUEST_EXECUTION_FAILED,
            OSDriveError.Code.NETWORK_FAILURE,
            OSDriveError.Code.BACKUP_REMOTE_ID_NOT_FOUND,
            OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED,
            OSDriveError.Code.UNEXPECTED_NULL_AUTH_INTENT,
            OSDriveError.Code.UNKNOWN_ERROR,
            null, // Retry by default
            -> true
            OSDriveError.Code.WRONG_ACCOUNT_TYPE,
            OSDriveError.Code.AUTHENTICATION_REQUIRED,
            OSDriveError.Code.UNEXPECTED_NULL_ACCOUNT,
            -> false
        }
        return canRetry
    }

    suspend fun onBackupWorkerSucceed(): Result {
        osNotificationManager.manager.cancel(OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID)
        autoBackupErrorRepository.setError(null)
        return Result.success()
    }

    companion object {
        private const val AUTO_BACKUP_SCHEDULER_WORK_NAME = "1d35209a-c713-439b-80a1-f83791018682"
        private const val RETRIES_BEFORE_SHOW_ERROR = 3
    }
}
