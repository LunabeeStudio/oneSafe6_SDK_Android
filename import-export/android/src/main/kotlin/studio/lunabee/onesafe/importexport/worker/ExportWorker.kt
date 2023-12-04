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
 * Last modified 10/17/23, 3:41 PM
 */

package studio.lunabee.onesafe.importexport.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.usecase.ExportBackupUseCase
import studio.lunabee.onesafe.importexport.utils.ForegroundInfoCompat
import timber.log.Timber
import java.io.File

/**
 * Worker that use [ExportBackupUseCase] to create a backup archive in private storage. It displays a foreground on going notification
 * while doing its job.
 */
@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val osNotificationManager: OSNotificationManager,
    @BackupType(BackupType.Type.Foreground) private val exportEngine: BackupExportEngine,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export) private val archiveDir: File,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        var workerResult = Result.success()
        exportBackupUseCase(exportEngine, archiveDir)
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
                        val data = Data.Builder()
                            .putString(FILE_OUTPUT_KEY, result.successData.file.path)
                            .build()
                        workerResult = Result.success(data)
                    }
                }
            }

        return workerResult
    }

    private suspend fun updateProgress(progress: Float) {
        val data = Data.Builder()
            .putFloat(PROGRESS_DATA_KEY, progress)
            .build()
        setProgress(data)
        val foregroundInfo = createForegroundInfo(progress)
        try {
            setForeground(foregroundInfo)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(0f)

    private fun createForegroundInfo(progress: Float): ForegroundInfo {
        Timber.d("Progress $progress") // TODO show progress

        val title = applicationContext.getString(R.string.export_progressCard_title)
        val notification = osNotificationManager.backupNotificationBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setOngoing(true)
            .build()

        return ForegroundInfoCompat.foregroundInfoDataSync(
            notificationId = OSNotificationManager.EXPORT_WORKER_NOTIFICATION_ID,
            notification = notification,
        )
    }

    companion object {
        private const val EXPORT_WORKER_NAME = "ded150c0-51f5-4d98-b572-243f6d4e3b55"

        private const val FILE_OUTPUT_KEY = "a35542e6-6003-4dd9-9267-0556e0c6bbf5"
        private const val PROGRESS_DATA_KEY = "375f2850-9884-4ef7-a50b-6e58be73a483"
        private const val ERROR_OUTPUT_KEY: String = "ab2c1e17-2b69-4839-b954-bf2b8a3fab73"

        fun start(context: Context, setExpedited: Boolean): Flow<LBFlowResult<File>> {
            val workRequestBuilder = OneTimeWorkRequestBuilder<ExportWorker>()
            if (setExpedited) {
                workRequestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
            val workRequest = workRequestBuilder.build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(EXPORT_WORKER_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)

            return workManager.getWorkInfoByIdFlow(workRequest.id).map { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING,
                    -> {
                        val progress = workInfo.progress.getFloat(PROGRESS_DATA_KEY, -1f)
                        LBFlowResult.Loading(progress = progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val finalPath = workInfo.outputData.getString(FILE_OUTPUT_KEY)!!
                        LBFlowResult.Success(File(finalPath))
                    }
                    WorkInfo.State.FAILED -> {
                        val message = workInfo.outputData.getString(ERROR_OUTPUT_KEY)
                            ?: OSAppError.Code.EXPORT_WORKER_FAILURE.message
                        LBFlowResult.Failure(OSAppError(OSAppError.Code.EXPORT_WORKER_FAILURE, message))
                    }
                    WorkInfo.State.CANCELLED -> LBFlowResult.Failure(OSAppError(OSAppError.Code.EXPORT_WORKER_CANCELED))
                }
            }
        }
    }
}
