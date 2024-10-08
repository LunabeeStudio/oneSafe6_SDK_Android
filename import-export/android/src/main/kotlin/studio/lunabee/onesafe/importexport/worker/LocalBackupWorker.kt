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
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.commonui.utils.setForegroundSafe
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.DeleteOldLocalBackupsUseCase
import studio.lunabee.onesafe.importexport.usecase.LocalAutoBackupUseCase
import studio.lunabee.onesafe.importexport.utils.ForegroundInfoCompat
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.jvm.toUUID

private val logger = LBLogger.get<LocalBackupWorker>()

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
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
    private val featureFlags: FeatureFlags,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val safeId = SafeId(inputData.getByteArray(EXPORT_WORKER_SAFE_ID_DATA)!!.toUUID())
        val flowResult = localAutoBackupUseCase(safeId)
            .catch { error ->
                emit(LBFlowResult.Failure(error))
            }.onStart {
                updateProgress(0f)
            }.onEach { result ->
                if (result is LBFlowResult.Loading) {
                    updateProgress(result.progress ?: -1f)
                }
            }.first {
                it !is LBFlowResult.Loading
            }

        return when (val result = flowResult.asResult()) {
            is LBResult.Failure -> autoBackupWorkersHelper.onBackupWorkerFails(
                error = result.throwable,
                runAttemptCount = runAttemptCount,
                errorSource = AutoBackupMode.LocalOnly,
                safeId = safeId,
            )
            is LBResult.Success -> {
                deleteOldBackupsUseCase(safeId)
                autoBackupWorkersHelper.onBackupWorkerSucceed(AutoBackupMode.LocalOnly, safeId)
            }
        }
    }

    private suspend fun updateProgress(progress: Float) {
        val data = Data.Builder()
            .putFloat(PROGRESS_DATA_KEY, progress)
            .build()
        setProgress(data)

        if (featureFlags.backupWorkerExpedited()) {
            setForegroundSafe(getForegroundInfo())
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(inputData.getFloat(PROGRESS_DATA_KEY, 0f))

    private fun createForegroundInfo(progress: Float): ForegroundInfo {
        logger.d("Progress $progress") // TODO show progress

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
        private const val PROGRESS_DATA_KEY: String = "375f2850-9884-4ef7-a50b-6e58be73a483"
        private const val EXPORT_WORKER_SAFE_ID_DATA = "c403bc67-67ea-42b9-b6df-e3ee6bf47a7f"

        internal fun getWorkRequest(setExpedited: Boolean, safeId: SafeId): OneTimeWorkRequest {
            val data = Data
                .Builder()
                .putByteArray(EXPORT_WORKER_SAFE_ID_DATA, safeId.id.toByteArray())
                .build()
            val workRequestBuilder = OneTimeWorkRequestBuilder<LocalBackupWorker>()
            workRequestBuilder
                .setInputData(data)
                .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(safeId))
            if (setExpedited) {
                workRequestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            return workRequestBuilder.build()
        }

        internal fun start(workManager: WorkManager, setExpedited: Boolean, safeId: SafeId) {
            workManager
                .enqueueUniqueWork(
                    ImportExportAndroidConstants.autoBackupWorkerName(safeId),
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    getWorkRequest(setExpedited, safeId),
                )
        }
    }
}
