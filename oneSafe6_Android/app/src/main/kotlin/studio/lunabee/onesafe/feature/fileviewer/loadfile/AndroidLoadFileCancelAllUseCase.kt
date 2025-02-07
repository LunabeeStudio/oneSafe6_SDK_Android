package studio.lunabee.onesafe.feature.fileviewer.loadfile

import androidx.work.WorkManager
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.repository.FileRepository
import java.util.UUID
import javax.inject.Inject

class AndroidLoadFileCancelAllUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val workManager: WorkManager,
) : LoadFileCancelAllUseCase {
    override suspend operator fun invoke(itemId: UUID) {
        fileRepository.deleteItemDir(itemId)
        LoadFileWorker.cancel(itemId, workManager)
    }

    override suspend operator fun invoke() {
        fileRepository.deletePlainFilesCacheDir()
        LoadFileWorker.cancelAll(workManager)
    }
}
