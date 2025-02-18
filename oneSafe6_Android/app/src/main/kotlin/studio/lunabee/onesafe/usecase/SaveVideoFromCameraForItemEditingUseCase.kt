package studio.lunabee.onesafe.usecase

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.use
import java.io.File
import javax.inject.Inject

class SaveVideoFromCameraForItemEditingUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val getThumbnailBitmapFromVideoFile: AndroidGetThumbnailFromFileUseCase,
    private val imageHelper: ImageHelper,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(videoFileToSave: File, encryptedPhotoCapture: InAppMediaCapture) {
        encryptedPhotoCapture.plainThumbnailFile?.let {
            storePlainThumbnail(videoFileToSave, it)
        }
        encryptedPhotoCapture.encryptedFile?.let {
            storeEncryptedFile(videoFileToSave, it)
        }
    }

    private suspend fun storePlainThumbnail(
        videoFileToSave: File,
        target: File,
    ) {
        val thumbnail: Bitmap = getThumbnailBitmapFromVideoFile.video(
            file = videoFileToSave,
            isFullWidth = false,
            displayDuration = false,
        ).imageSpec.getAs()
        val thumbnailData = imageHelper.convertBitmapToByteArray(thumbnail)
        thumbnail.recycle()
        withContext(fileDispatcher) {
            thumbnailData.use { data ->
                data.inputStream().use { inputStream ->
                    target.parentFile?.mkdirs()
                    target.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private suspend fun storeEncryptedFile(
        srcFile: File,
        target: File,
    ) {
        withContext(fileDispatcher) {
            target.parentFile?.mkdirs()
            val encryptStream = cryptoRepository.getFileEditionEncryptStream(target)
            srcFile.inputStream().use { inputStream ->
                encryptStream.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            srcFile.delete()
        }
    }
}
