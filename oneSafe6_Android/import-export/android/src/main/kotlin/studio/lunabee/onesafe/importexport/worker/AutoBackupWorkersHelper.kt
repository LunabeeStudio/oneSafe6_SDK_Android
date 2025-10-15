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
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.usecase.ClearAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.StoreAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.utils.AutoBackupErrorIdProvider
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper.Companion.RetriesBeforeShowError
import studio.lunabee.onesafe.jvm.toByteArray
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

private val logger = LBLogger.get<AutoBackupWorkersHelper>()

class AutoBackupWorkersHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
    private val osNotificationManager: OSNotificationManager,
    private val clock: Clock,
    private val storeAutoBackupErrorUseCase: StoreAutoBackupErrorUseCase,
    private val clearAutoBackupErrorUseCase: ClearAutoBackupErrorUseCase,
    private val autoBackupErrorIdProvider: AutoBackupErrorIdProvider,
    private val safeRepository: SafeRepository,
    private val workManager: WorkManager,
) {
    suspend fun start(
        synchronizeCloudFirst: Boolean,
        safeId: SafeId? = null,
    ) {
        val backupSafeId = safeId ?: safeRepository.currentSafeId()
        val data = Data
            .Builder()
            .putBoolean(AutoBackupSchedulerWorker.SynchronizeCloudFirstData, synchronizeCloudFirst)
            .putByteArray(AutoBackupSchedulerWorker.ExportWorkerSafeIdData, backupSafeId.id.toByteArray())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AutoBackupSchedulerWorker>()
            .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(backupSafeId))
            .setInputData(data)
            .build()

        workManager
            .enqueueUniqueWork(AutoBackupSchedulerWorkName, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)
    }

    suspend fun cancel(
        safeId: SafeId? = null,
    ) {
        val backupSafeId = safeId ?: safeRepository.currentSafeId()
        workManager.cancelAllWorkByTag(ImportExportAndroidConstants.autoBackupWorkerTag(backupSafeId))
    }

    private suspend fun isScheduled(safeId: SafeId): Boolean = workManager
        .getWorkInfosForUniqueWorkFlow(AutoBackupSchedulerWorker.autoBackupChainWorkName(safeId))
        .first()
        .firstOrNull()
        ?.state in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)

    suspend fun ensureAutoBackupScheduled() {
        autoBackupSettingsRepository.getSafeAutoBackupEnabled().forEach { backupEnabledStatus ->
            if (!isScheduled(backupEnabledStatus.safeId)) {
                logger.w("Re-scheduled auto backup worker")
                start(backupEnabledStatus.cloudAutoBackupEnabled, backupEnabledStatus.safeId)
            }
        }
    }

    /**
     * TODO permission should be add by the module if it needs it
     * Handle backup worker error
     *  • set backup error and send notification if [runAttemptCount] reaches [RetriesBeforeShowError]
     *  • compute worker result depending on the [error] and [runAttemptCount]
     *
     * @param error Error caught in backup worker
     * @param runAttemptCount see ListenableWorker.getRunAttemptCount
     * @return The worker [Result]
     */
    @SuppressLint("MissingPermission")
    suspend fun onBackupWorkerFails(error: Throwable?, runAttemptCount: Int, errorSource: AutoBackupMode, safeId: SafeId): Result {
        logger.e("fail #$runAttemptCount", error)
        val canRetry = canRetry(error)

        if (runAttemptCount == RetriesBeforeShowError || !canRetry) {
            // Store error
            val autoBackupError = AutoBackupError(
                id = autoBackupErrorIdProvider(),
                date = ZonedDateTime.now(clock),
                code = error.codeText().string(context),
                message = error?.stackTraceToString(),
                source = errorSource,
                safeId = safeId,
            )
            storeAutoBackupErrorUseCase(autoBackupError)
            // Notify
            if (osNotificationManager.areNotificationsEnabled(OSNotificationChannelId.BACKUP_CHANNEL_ID)) {
                val title = context.getString(OSString.notification_autobackup_error_title)
                val message = context
                    .getString(OSString.notification_autobackup_error_message, error.codeText().string(context))
                val notificationBuilder = osNotificationManager.backupNotificationBuilder
                    .setContentTitle(title)
                    .setStyle(
                        NotificationCompat
                            .BigTextStyle()
                            .bigText(message),
                    ).setTicker(title)
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
                    OSNotificationManager.AutoBackupErrorWorkerNotificationId,
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
            OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED,
            OSDriveError.Code.DRIVE_NETWORK_FAILURE,
            OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND,
            OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED,
            OSDriveError.Code.DRIVE_UNEXPECTED_NULL_AUTH_INTENT,
            OSDriveError.Code.DRIVE_UNKNOWN_ERROR,
            OSDriveError.Code.DRIVE_CANNOT_DELETE_BACKUP_WITHOUT_SAFE_ID,
            null, // Retry by default
            -> true
            OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE,
            OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED,
            OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT,
            -> false
        }
        return canRetry
    }

    suspend fun onBackupWorkerSucceed(backupMode: AutoBackupMode, safeId: SafeId): Result {
        osNotificationManager.manager.cancel(OSNotificationManager.AutoBackupErrorWorkerNotificationId)
        clearAutoBackupErrorUseCase.ifNeeded(safeId, backupMode)
        return Result.success()
    }

    companion object {
        private const val AutoBackupSchedulerWorkName = "1d35209a-c713-439b-80a1-f83791018682"
        private const val RetriesBeforeShowError = 3
    }
}
