package studio.lunabee.onesafe.usecase

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.use
import java.io.File
import javax.inject.Inject

/**
 * Takes an image, create a thumbnail of it and saves the original full size image in an encrypted file with ItemEdition cryptographic key
 */
class SaveImageFromCameraForItemEditingUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val imageHelper: ImageHelper,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(imageToSave: Bitmap, encryptedPhotoCapture: InAppMediaCapture) {
        encryptedPhotoCapture.plainThumbnailFile?.let {
            storePlainThumbnail(imageToSave, it)
        }
        encryptedPhotoCapture.encryptedFile?.let {
            storeEncryptedImage(imageToSave, it)
        }
    }

    private suspend fun storePlainThumbnail(
        imageToSave: Bitmap,
        target: File,
    ) {
        val thumbnail = withContext(defaultDispatcher) {
            ThumbnailUtils.extractThumbnail(
                imageToSave,
                AndroidGetThumbnailFromFileUseCase.thumbnailWidth,
                AndroidGetThumbnailFromFileUseCase.thumbnailHeight,
            )
        }
        val thumbnailData = imageHelper.convertBitmapToByteArray(thumbnail)
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

    private suspend fun storeEncryptedImage(
        imageToSave: Bitmap,
        target: File,
    ) {
        val imageData = imageHelper.convertBitmapToByteArray(imageToSave)
        target.parentFile?.mkdirs()
        val encryptStream = cryptoRepository.getFileEditionEncryptStream(target)
        withContext(fileDispatcher) {
            imageData.use { data ->
                data.inputStream().use { inputStream ->
                    encryptStream.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }
}
