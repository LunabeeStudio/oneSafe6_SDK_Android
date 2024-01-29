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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.commonui.utils.setForegroundSafe
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.usecase.CloudAutoBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.DeleteOldCloudBackupsUseCase
import studio.lunabee.onesafe.importexport.utils.ForegroundInfoCompat
import com.lunabee.lblogger.LBLogger

// TODO <AutoBackup> split cloudAutoBackupUseCase with zip and upload part so the worker can chain them to have finer control over the flow

private val logger = LBLogger.get<CloudBackupWorker>()

/**
 * Run [CloudAutoBackupUseCase] and [DeleteOldCloudBackupsUseCase] to execute a cloud backup
 */
@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cloudAutoBackupUseCase: CloudAutoBackupUseCase,
    private val deleteOldBackupsUseCase: DeleteOldCloudBackupsUseCase,
    private val osNotificationManager: OSNotificationManager,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    private val featureFlags: FeatureFlags,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (featureFlags.backupWorkerExpedited()) {
            setForegroundSafe(getForegroundInfo())
        }

        val flowResult = cloudAutoBackupUseCase().transformResult {
            emitAll(deleteOldBackupsUseCase())
        }.catch { error ->
            emit(LBFlowResult.Failure(error))
        }.onEach { result ->
            if (result is LBFlowResult.Loading) {
                logger.v("Progress ${result.progress}")
            }
        }.first {
            it !is LBFlowResult.Loading
        }

        return when (val result = flowResult.asResult()) {
            is LBResult.Failure -> autoBackupWorkersHelper.onBackupWorkerFails(result.throwable, runAttemptCount)
            is LBResult.Success -> autoBackupWorkersHelper.onBackupWorkerSucceed()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title = applicationContext.getString(OSString.notification_autobackup_progress_title)
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
        private fun getWorkRequest(setExpedited: Boolean): OneTimeWorkRequest {
            val workRequestBuilder = OneTimeWorkRequestBuilder<CloudBackupWorker>()
                .addTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build(),
                )
            if (setExpedited) {
                workRequestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            return workRequestBuilder.build()
        }

        internal fun start(context: Context, setExpedited: Boolean) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    ImportExportAndroidConstants.AUTO_BACKUP_WORKER_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    getWorkRequest(setExpedited),
                )
        }
    }
}
