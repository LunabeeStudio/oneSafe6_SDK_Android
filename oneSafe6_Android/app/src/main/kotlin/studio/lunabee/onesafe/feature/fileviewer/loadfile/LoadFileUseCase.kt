package studio.lunabee.onesafe.feature.fileviewer.loadfile

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import java.io.File
import javax.inject.Inject

class LoadFileUseCase @Inject constructor(
    private val workManager: WorkManager,
) {
    operator fun invoke(
        safeItemField: SafeItemField,
    ): Flow<LBFlowResult<File>> {
        val workers = workManager.getWorkInfosByTag(safeItemField.id.toString()).get()
        val id = workers.firstOrNull { !it.state.isFinished }?.id ?: LoadFileWorker.start(
            safeItemField.itemId,
            safeItemField.id,
            workManager,
        )
        return workManager.getWorkInfoByIdFlow(id).map { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
                -> {
                    LBFlowResult.Loading(null)
                }
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED,
                -> {
                    val error = workInfo.outputData.getString(LoadFileWorker.ERROR_DATA)
                    LBFlowResult.Failure(message = error.orEmpty())
                }
                WorkInfo.State.SUCCEEDED -> {
                    val path = workInfo.outputData.getString(LoadFileWorker.FILE_PATH_DATA)
                    path?.let { LBFlowResult.Success(File(path)) } ?: LBFlowResult.Failure()
                }
                null -> LBFlowResult.Loading()
            }
        }
    }
}
