package studio.lunabee.onesafe.feature.fileviewer.loadfile

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
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.usecase.GetPlainFileUseCase
import java.util.UUID

@HiltWorker
class LoadFileWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getPlainFileUseCase: GetPlainFileUseCase,
    private val safeItemFieldRepository: SafeItemFieldRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fieldId = inputData.getString(FIELD_ID_DATA)
        val field = safeItemFieldRepository.getSafeItemField(UUID.fromString(fieldId))
        val result = getPlainFileUseCase(field)
        return when (result) {
            is LBResult.Failure -> Result.failure(
                Data.Builder().putString(ERROR_DATA, result.throwable.description().string(applicationContext)).build(),
            )
            is LBResult.Success -> Result.success(
                Data.Builder().putString(FILE_PATH_DATA, result.successData.path).build(),
            )
        }
    }

    companion object {
        private const val FIELD_ID_DATA: String = "71fad4c2-0af1-44ed-b658-9dffbe218cb8"
        const val FILE_PATH_DATA: String = "146b3989-f207-4458-8eac-c5f10cc228c2"
        const val ERROR_DATA: String = "d48b5bea-992c-497e-9523-73437bf900c2"

        fun start(itemId: UUID, fieldId: UUID, workManager: WorkManager): UUID {
            val data = Data.Builder()
                .putString(FIELD_ID_DATA, fieldId.toString())
                .build()
            val workRequest = OneTimeWorkRequestBuilder<LoadFileWorker>()
                .addTag(itemId.toString())
                .addTag(fieldId.toString())
                .setInputData(data)
                .build()
            workManager.enqueueUniqueWork(
                LoadFileWorker::class.java.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest,
            )
            return workRequest.id
        }

        fun cancel(itemId: UUID, workManager: WorkManager) {
            workManager.cancelAllWorkByTag(itemId.toString())
        }

        fun cancelAll(workManager: WorkManager) {
            workManager.cancelUniqueWork(LoadFileWorker::class.java.name)
        }
    }
}
