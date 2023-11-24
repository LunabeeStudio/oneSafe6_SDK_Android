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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.error.code
import studio.lunabee.onesafe.commonui.notification.OSNotificationChannelId
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import timber.log.Timber
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

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
            Timber.w("Re-scheduled auto backup worker")
            start(autoBackupSettingsRepository.cloudBackupEnabled.first())
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun onBackupWorkerFails(error: Throwable?, runAttemptCount: Int): Result {
        val canRetry = (error as? OSDriveError)?.code?.canRetry ?: false

        if (runAttemptCount == 3 || !canRetry) {
            // Store error
            val autoBackupError = AutoBackupError(
                date = ZonedDateTime.now(clock),
                code = error.code().string(context),
                message = error?.stackTraceToString(),
            )
            autoBackupErrorRepository.setError(autoBackupError)
            // Notify
            if (osNotificationManager.areNotificationsEnabled(OSNotificationChannelId.BACKUP_CHANNEL_ID)) {
                val title = context.getString(R.string.notification_autobackup_error_title)
                val message = context.getString(R.string.notification_autobackup_error_message, error.code().string(context))
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

    suspend fun onBackupWorkerSucceed(): Result {
        osNotificationManager.manager.cancel(OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID)
        autoBackupErrorRepository.setError(null)
        return Result.success()
    }

    companion object {
        private const val AUTO_BACKUP_SCHEDULER_WORK_NAME = "1d35209a-c713-439b-80a1-f83791018682"
    }
}