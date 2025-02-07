/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 16:24
 */

package studio.lunabee.onesafe.login

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lunabee.lbcore.model.LBResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import studio.lunabee.onesafe.domain.repository.WorkerCryptoRepository
import studio.lunabee.onesafe.domain.usecase.autodestruction.ExecuteAutoDestructionUseCase

@HiltWorker
class AutoDestructionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val executeAutoDestructionUseCase: ExecuteAutoDestructionUseCase,
    private val workerCryptoRepository: WorkerCryptoRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val encPassword = inputData.getByteArray(PASSWORD_DATA) ?: return Result.failure()
        val plainPassword = workerCryptoRepository.decrypt(encPassword)
        return when (executeAutoDestructionUseCase(plainPassword)) {
            is LBResult.Failure -> Result.failure()
            is LBResult.Success -> Result.success()
        }
    }

    companion object {
        private const val PASSWORD_DATA: String = "1862f1d1-224c-4f09-98cf-80ad1b49e8ac"

        fun start(encPassword: ByteArray, workManager: WorkManager) {
            val data = Data.Builder().putByteArray(PASSWORD_DATA, encPassword).build()
            val workRequest = OneTimeWorkRequestBuilder<AutoDestructionWorker>()
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(
                AutoDestructionWorker::class.java.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest,
            )
        }
    }
}
