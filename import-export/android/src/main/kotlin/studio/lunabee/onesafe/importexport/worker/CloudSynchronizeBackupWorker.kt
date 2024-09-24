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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.SynchronizeCloudBackupsUseCase
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.jvm.toUUID

private val logger = LBLogger.get<CloudSynchronizeBackupWorker>()

/**
 * Worker that synchronize local backups with cloud by calling [SynchronizeCloudBackupsUseCase]
 */
@HiltWorker
class CloudSynchronizeBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val synchronizeCloudBackupsUseCase: SynchronizeCloudBackupsUseCase,
    private val autoBackupWorkersHelper: AutoBackupWorkersHelper,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val safeId = SafeId(inputData.getByteArray(EXPORT_WORKER_SAFE_ID_DATA)!!.toUUID())
        val flowResult = synchronizeCloudBackupsUseCase(safeId)
            .catch { error ->
                emit(LBFlowResult.Failure(error))
            }.onEach { result ->
                if (result is LBFlowResult.Loading) {
                    logger.v("Progress ${result.progress}")
                }
            }.first {
                it !is LBFlowResult.Loading
            }

        return when (val result = flowResult.asResult()) {
            is LBResult.Failure -> autoBackupWorkersHelper.onBackupWorkerFails(
                error = result.throwable,
                runAttemptCount = runAttemptCount,
                errorSource = AutoBackupMode.Synchronized,
                safeId = safeId,
            )
            is LBResult.Success -> autoBackupWorkersHelper.onBackupWorkerSucceed(AutoBackupMode.Synchronized, safeId)
        }
    }

    companion object {
        private const val EXPORT_WORKER_SAFE_ID_DATA = "4090613a-696e-4aaa-a13e-67ba0c272301"

        fun getWorkRequest(safeId: SafeId): OneTimeWorkRequest {
            val data = Data
                .Builder()
                .putByteArray(EXPORT_WORKER_SAFE_ID_DATA, safeId.id.toByteArray())
                .build()
            return OneTimeWorkRequestBuilder<CloudSynchronizeBackupWorker>()
                .addTag(ImportExportAndroidConstants.autoBackupWorkerTag(safeId))
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build(),
                )
                .build()
        }
    }
}
