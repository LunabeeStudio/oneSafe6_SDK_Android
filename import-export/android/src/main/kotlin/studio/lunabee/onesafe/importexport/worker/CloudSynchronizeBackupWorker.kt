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
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.usecase.SynchronizeCloudBackupsUseCase
import timber.log.Timber

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
        val flowResult = synchronizeCloudBackupsUseCase()
            .catch { error ->
                emit(LBFlowResult.Failure(error))
            }.onEach { result ->
                if (result is LBFlowResult.Loading) {
                    Timber.v("Progress ${result.progress}")
                }
            }.first {
                it !is LBFlowResult.Loading
            }

        return when (val result = flowResult.asResult()) {
            is LBResult.Failure -> autoBackupWorkersHelper.onBackupWorkerFails(result.throwable, runAttemptCount)
            is LBResult.Success -> autoBackupWorkersHelper.onBackupWorkerSucceed()
        }
    }

    companion object {
        fun getWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CloudSynchronizeBackupWorker>()
                .addTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build(),
                )
                .build()
        }
    }
}
