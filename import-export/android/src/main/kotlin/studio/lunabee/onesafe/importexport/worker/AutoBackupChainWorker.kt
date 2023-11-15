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
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupModeUseCase
import timber.log.Timber

// TODO <AutoBackup> test (manually, maybe unit test?)

/**
 * Launch the auto backup worker depending on user settings [AutoBackupMode]
 */
@HiltWorker
class AutoBackupChainWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val itemRepository: SafeItemRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (itemRepository.getSafeItemsCount() != 0) {
            when (val backupMode = getAutoBackupModeUseCase()) {
                AutoBackupMode.DISABLED -> {
                    // Unexpected, log and cancel workers
                    Timber.e("${AutoBackupChainWorker::class.simpleName} run but ${AutoBackupMode::class.simpleName} is $backupMode")
                    AutoBackupSchedulerWorker.cancel(applicationContext)
                }
                AutoBackupMode.LOCAL_ONLY -> LocalBackupWorker.start(applicationContext)
                AutoBackupMode.CLOUD_ONLY -> CloudBackupWorker.start(applicationContext)
                AutoBackupMode.SYNCHRONIZED -> {
                    val localWorkRequest = LocalBackupWorker.getWorkRequest()
                    val cloudSyncWorkRequest = CloudSynchronizeBackupWorker.getWorkRequest()

                    WorkManager.getInstance(applicationContext)
                        .beginUniqueWork(
                            AUTO_BACKUP_UNIQUE_CHAIN_WORK_NAME,
                            ExistingWorkPolicy.APPEND_OR_REPLACE,
                            localWorkRequest,
                        )
                        .then(cloudSyncWorkRequest)
                        .enqueue()
                }
            }
        }

        return Result.success()
    }

    companion object {
        const val AUTO_BACKUP_UNIQUE_CHAIN_WORK_NAME: String = "d725ac85-8742-46db-930d-7903470d05a6"
    }
}
