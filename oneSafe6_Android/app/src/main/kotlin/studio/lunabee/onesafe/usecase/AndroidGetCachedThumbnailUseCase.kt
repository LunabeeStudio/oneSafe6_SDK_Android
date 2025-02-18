package studio.lunabee.onesafe.usecase

import android.content.Context
import android.webkit.MimeTypeMap
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.model.FileThumbnailData
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AndroidGetCachedThumbnailUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoRepository: MainCryptoRepository,
    private val loadFileUseCase: LoadFileUseCase,
    private val decryptUseCase: ItemDecryptUseCase,
    private val fileIdProvider: FileIdProvider,
    private val androidGetThumbnailUseCase: AndroidGetThumbnailFromFileUseCase,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val fileRepository: FileRepository,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
    private val imageHelper: ImageHelper,
) {
    /**
     * Return the thumbnail related to the related field
     * If the file mimeType is not managed, we return the place holder directly
     * If the mimeType is managed, we either generate the image and store it, or return the already generated one
     */
    suspend operator fun invoke(
        safeItemField: SafeItemField,
        isFullWidth: Boolean = false,
    ): OSImageSpec {
        val itemKey = safeItemKeyRepository.getSafeItemKey(safeItemField.itemId)
        // If the  extension is not suppose to be supported, we return directly the placeholder, is case it becomes supported in the future
        val mimeType = safeItemField.encValue?.let {
            val value = decryptUseCase(it, itemKey, String::class)
            val fileExtension = value.data?.substringAfter(Constant.FileTypeExtSeparator).orEmpty()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        }
        return when {
            mimeType?.contains(AppConstants.FileProvider.ImageMimeTypePrefix) == true ||
                mimeType?.contains(AppConstants.FileProvider.VideoMimeTypePrefix) == true ||
                mimeType?.contains(AppConstants.FileProvider.AudioMimeTypePrefix) == true ||
                mimeType?.contains(AppConstants.FileProvider.PdfExtension) == true ->
                getOrGenerateThumbnail(
                    safeItemField,
                    isFullWidth,
                    itemKey,
                ).imageSpec
            else -> return FileThumbnailData.FileThumbnailPlaceholder.File.imageSpec
        }
    }

    /**
     * Get the thumbnail if already generated or generate it.
     * thumbnail name is either:
     * - a UUID stored in SafeItemField encrypted with itemKey if the data is an image
     * - a FileThumbnailPlaceholder id if thumbnail is a resource
     */
    private suspend fun getOrGenerateThumbnail(
        safeItemField: SafeItemField,
        isFullWidth: Boolean = false,
        itemKey: SafeItemKey,
    ): FileThumbnailData {
        val thumbnailFileName = safeItemField.encThumbnailFileName?.let {
            decryptUseCase(
                it,
                itemKey,
                UUID::class,
            )
        }?.data.takeIf { it != Constant.ThumbnailPlaceHolderName }
            ?: fileIdProvider()
        // If thumbnail is resource, return it directly
        FileThumbnailData.FileThumbnailPlaceholder.entries.firstOrNull { it.id == thumbnailFileName }?.let {
            return it
        }
        val thumbnailFile = fileRepository.getThumbnailFile(thumbnailFileName.toString(), isFullWidth)
        return if (thumbnailFile.exists()) {
            withContext(fileDispatcher) {
                FileThumbnailData.Data(
                    OSImageSpec.Data(cryptoRepository.getDecryptStream(cipherFile = thumbnailFile, key = itemKey).readBytes()),
                )
            }
        } else {
            val plainFile = loadFileUseCase(safeItemField).first { it is LBFlowResult.Success }.data
            plainFile?.let {
                val thumbnail = androidGetThumbnailUseCase(plainFile, isFullWidth)
                when (thumbnail) {
                    is FileThumbnailData.Data -> saveThumbnailFile(
                        thumbnail = thumbnail,
                        thumbnailFile = thumbnailFile,
                        itemKey = itemKey,
                        thumbnailFileName = thumbnailFileName,
                        safeItemFieldId = safeItemField.id,
                    )
                    is FileThumbnailData.FileThumbnailPlaceholder -> saveThumbnailResource(
                        safeItemFieldId = safeItemField.id,
                        itemKey = itemKey,
                        thumbnail = thumbnail,
                    )
                }
                thumbnail
            } ?: FileThumbnailData.FileThumbnailPlaceholder.File
        }
    }

    private suspend fun saveThumbnailResource(
        safeItemFieldId: UUID,
        itemKey: SafeItemKey,
        thumbnail: FileThumbnailData.FileThumbnailPlaceholder,
    ) {
        val encFileName = cryptoRepository.encrypt(itemKey, EncryptEntry(thumbnail.id))
        safeItemFieldRepository.saveThumbnailFileName(fieldId = safeItemFieldId, encFileName)
    }

    private suspend fun saveThumbnailFile(
        thumbnail: FileThumbnailData.Data,
        thumbnailFile: File,
        itemKey: SafeItemKey,
        thumbnailFileName: UUID,
        safeItemFieldId: UUID,
    ) {
        withContext(fileDispatcher) {
            cryptoRepository.getEncryptStream(thumbnailFile, key = itemKey).use { stream ->
                imageHelper.convertImageSpecToByteArray(thumbnail.imageSpec, context)?.inputStream()?.use {
                    it.copyTo(stream)
                }
            }
        }
        val encFileName = cryptoRepository.encrypt(itemKey, EncryptEntry(thumbnailFileName))
        safeItemFieldRepository.saveThumbnailFileName(fieldId = safeItemFieldId, encFileName)
    }
}
