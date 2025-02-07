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
 * Created by Lunabee Studio / Date - 9/19/2024 - for the oneSafe6 SDK.
 * Last modified 19/09/2024 10:38
 */

package studio.lunabee.onesafe.widget.panic.worker

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
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
import studio.lunabee.onesafe.domain.usecase.IsPanicWidgetEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.ExecutePanicDestructionUseCase
import studio.lunabee.onesafe.widget.panic.PanicButtonWidget
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetState
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetStateDefinition
import studio.lunabee.onesafe.widget.panic.state.PanicWidgetWorkerState

@HiltWorker
internal class MainPanicWidgetWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val isPanicWidgetEnabledUseCase: IsPanicWidgetEnabledUseCase,
    private val executePanicDestructionUseCase: ExecutePanicDestructionUseCase,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val widgetId = inputData.getInt(WidgetId, -1)
        val glanceAppWidgetManager = GlanceAppWidgetManager(context)
        val isPanicClick = inputData.getBoolean(IsPanicDestruction, false)
        val glanceId: GlanceId = glanceAppWidgetManager.getGlanceIdBy(widgetId)
        val isWidgetEnabled = isPanicWidgetEnabledUseCase()
        return when {
            isWidgetEnabled && isPanicClick -> deleteAllSafe(glanceId)
            isWidgetEnabled -> {
                val newWidgetWorkerState = PanicWidgetWorkerState.Idle
                updateUiWidgetState(
                    glanceId = glanceId,
                    newState = newWidgetWorkerState,
                )
                Result.success()
            }
            else -> {
                val newWidgetWorkerState = PanicWidgetWorkerState.Disabled
                updateUiWidgetState(
                    glanceId = glanceId,
                    newState = newWidgetWorkerState,
                )
                Result.success()
            }
        }
    }

    private suspend fun deleteAllSafe(glanceId: GlanceId): Result {
        updateUiWidgetState(
            glanceId = glanceId,
            newState = PanicWidgetWorkerState.Loading,
        )
        return when (executePanicDestructionUseCase()) {
            is LBResult.Failure -> {
                val newWidgetWorkerState = PanicWidgetWorkerState.Idle
                updateUiWidgetState(
                    glanceId = glanceId,
                    newState = newWidgetWorkerState,
                )
                Result.failure()
            }
            is LBResult.Success -> {
                val newWidgetWorkerState = PanicWidgetWorkerState.Disabled
                updateUiWidgetState(
                    glanceId = glanceId,
                    newState = newWidgetWorkerState,
                )
                Result.success()
            }
        }
    }

    private suspend fun updateUiWidgetState(glanceId: GlanceId, newState: PanicWidgetWorkerState) {
        updateAppWidgetState(
            context = context,
            definition = PanicWidgetStateDefinition,
            glanceId = glanceId,
            updateState = { PanicWidgetState.fromWidgetWorkerState(newState) },
        )
        PanicButtonWidget().update(context = context, id = glanceId)
    }

    companion object {
        private const val WidgetId: String = "71fad4c2-0af1-44ed-b658-9dffbe218cb8"
        private const val IsPanicDestruction: String = "f0db3709-3736-4d87-a407-5d3cf5e5e2ca"

        fun start(widgetId: Int, workManager: WorkManager, isPanicDestruction: Boolean) {
            val data = Data.Builder()
                .putInt(WidgetId, widgetId)
                .putBoolean(IsPanicDestruction, isPanicDestruction)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<MainPanicWidgetWorker>()
                .addTag("${MainPanicWidgetWorker::class.java.name}_$widgetId")
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(
                "${MainPanicWidgetWorker::class.java.name}_$widgetId",
                ExistingWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
