package studio.lunabee.onesafe.feature.itemform.manager

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.model.FileThumbnailData
import studio.lunabee.onesafe.common.utils.FileSerializer
import studio.lunabee.onesafe.common.utils.NullableFileSerializer
import studio.lunabee.onesafe.common.utils.UriSerializer
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.extension.getMimeType
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.commonui.utils.FileDetails
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.itemform.model.FileToLargeDialogState
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.CaptureFileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FromUriFileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ThumbnailState
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.usecase.AndroidGetThumbnailFromFileUseCase
import java.io.File
import javax.inject.Inject

private val logger = LBLogger.get<ItemEditionFileFieldManager>()

/**
 * Manage files for field edition
 */
@ViewModelScoped
class ItemEditionFileFieldManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileIdProvider: FileIdProvider,
    private val itemIdProvider: ItemIdProvider,
    private val getThumbnailFromFileUseCase: AndroidGetThumbnailFromFileUseCase,
    private val cryptoRepository: MainCryptoRepository,
    private val getAppSettingUseCase: GetAppSettingUseCase,
) : CloseableCoroutineScope by CloseableMainCoroutineScope() {
    companion object {
        private const val THUMBNAIL_DIR: String = "edition_thumbnail"
        private const val IMAGE_CAPTURE_DIR: String = "edition_capture"
        private const val ENCRYPTED_CACHE_DIR: String = "edition_encrypted"
    }

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> = _dialogState.asStateFlow()

    private val thumbnailCacheDir: File = File(context.cacheDir, THUMBNAIL_DIR).also {
        it.mkdirs()
        it.deleteOnExit()
    }

    private val imageCaptureCacheDir: File = File(context.cacheDir, IMAGE_CAPTURE_DIR).also {
        it.mkdirs()
        it.deleteOnExit()
    }

    private val encryptedCacheDir: File = File(context.cacheDir, ENCRYPTED_CACHE_DIR).also {
        it.mkdirs()
        it.deleteOnExit()
    }

    fun createItemFileField(uri: Uri): FromUriFileUiField? {
        val fileDetails = FileDetails.fromUri(uri, context)
        if ((fileDetails?.size ?: 0) > Constant.FileMaxSizeBytes) {
            _dialogState.value = FileToLargeDialogState { _dialogState.value = null }
            return null
        }

        val mimeType = uri.getMimeType(context)
        val safeItemFieldKind: SafeItemFieldKind = when {
            mimeType?.startsWith(AppConstants.FileProvider.ImageMimeTypePrefix) == true -> SafeItemFieldKind.Photo
            mimeType?.startsWith(AppConstants.FileProvider.VideoMimeTypePrefix) == true -> SafeItemFieldKind.Video
            else -> SafeItemFieldKind.File
        }
        val fieldId = itemIdProvider()
        val fileId = fileIdProvider()
        val fileName = fileDetails?.name ?: fieldId.toString()
        val thumbnailFlow: MutableStateFlow<ThumbnailState> = MutableStateFlow(ThumbnailState.Loading)
        coroutineScope.launch(Dispatchers.IO) {
            val thumbnail = try {
                context.contentResolver.openInputStream(uri)?.let { inputStream ->
                    val tempFile = File(thumbnailCacheDir, fileName)
                    tempFile.parentFile?.mkdirs()
                    inputStream.use {
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    val thumbnail = getThumbnailFromFileUseCase(tempFile, displayDuration = false)
                    tempFile.delete()
                    thumbnail
                }
            } catch (e: Throwable) {
                logger.e(e)
                null
            } ?: FileThumbnailData.FileThumbnailPlaceholder.File
            thumbnailFlow.value = ThumbnailState.Finished(thumbnail.imageSpec)
        }
        return FromUriFileUiField(
            id = fieldId,
            safeItemFieldKind = safeItemFieldKind,
            fileName = fileName,
            fileExtension = fileDetails?.extension,
            thumbnailFlow = thumbnailFlow,
            getInputStream = { context.contentResolver.openInputStream(uri) },
            fileId = fileId,
        )
    }

    private fun manageImageCapturedFromInAppCamera(
        numberOfMedia: Int,
        encryptedPhotoCapture: InAppMediaCapture,
    ): LBResult<CaptureFileUiField> {
        val name = LbcTextSpec.StringResource(encryptedPhotoCapture.mediaType.stringNameRes, numberOfMedia + 1)
            .string(context) + ".${encryptedPhotoCapture.mediaType.extension}"

        return if (encryptedPhotoCapture.plainThumbnailFile?.exists() == true && encryptedPhotoCapture.encryptedFile?.exists() == true) {
            // Generate thumbnail bitmap
            val thumbnail = BitmapFactory.decodeFile(encryptedPhotoCapture.plainThumbnailFile.path)
            val thumbnailFlow: StateFlow<ThumbnailState> = MutableStateFlow(
                ThumbnailState.Finished(OSImageSpec.Bitmap(thumbnail)),
            ).asStateFlow()

            // Create the encrypted file with itemEdition Key
            val fileId = fileIdProvider()
            val encFile = File(encryptedCacheDir, fileId.toString()).also { it.parentFile?.mkdirs() }
            encryptedPhotoCapture.encryptedFile.copyTo(encFile)

            // Delete the capture files
            encryptedPhotoCapture.delete()

            LBResult.Success(
                CaptureFileUiField(
                    thumbnailFlow = thumbnailFlow,
                    id = fileIdProvider(),
                    fileId = fileId,
                    fileName = name,
                    getPlainInputStream = { cryptoRepository.getFileEditionDecryptStream(encFile) },
                    file = encFile,
                    fileExtension = encryptedPhotoCapture.mediaType.extension,
                    safeItemFieldKind = when (encryptedPhotoCapture.mediaType) {
                        OSMediaType.PHOTO -> SafeItemFieldKind.Photo
                        OSMediaType.VIDEO -> SafeItemFieldKind.Video
                    },
                ),
            )
        } else {
            LBResult.Failure(OSAppError.Code.IMAGE_CAPTURED_NOT_FOUND.get())
        }
    }

    suspend fun manageMediaCaptured(numberOfImage: Int, cameraData: CameraData): LBResult<CaptureFileUiField> = when (cameraData) {
        is CameraData.External -> {
            val mediaCaptureFile = cameraData.photoCapture.value.file
            manageImageCaptureFromSystemCameraResult(numberOfImage, mediaCaptureFile)
        }
        is CameraData.InApp -> manageImageCapturedFromInAppCamera(numberOfImage, cameraData.photoCapture)
    }

    private suspend fun manageImageCaptureFromSystemCameraResult(numberOfImage: Int, imageCaptureFile: File): LBResult<CaptureFileUiField> {
        val name = LbcTextSpec.StringResource(OSString.fieldName_photo_count, numberOfImage + 1)
            .string(context) + ".${OSMediaType.PHOTO.extension}"

        return if (imageCaptureFile.exists()) {
            // Generate thumbnail from image
            val thumbnail = getThumbnailFromFileUseCase(imageCaptureFile).imageSpec
            val thumbnailFlow: StateFlow<ThumbnailState> = MutableStateFlow(ThumbnailState.Finished(thumbnail)).asStateFlow()

            // Create the encrypted file with itemEdition Key
            val fileId = fileIdProvider()
            val encFile = File(encryptedCacheDir, fileId.toString()).also { it.parentFile?.mkdirs() }
            cryptoRepository.getFileEditionEncryptStream(encFile).use { encOutputStream ->
                imageCaptureFile.inputStream().use { inputStream ->
                    inputStream.copyTo(encOutputStream)
                }
            }

            imageCaptureFile.delete()

            LBResult.Success(
                CaptureFileUiField(
                    thumbnailFlow = thumbnailFlow,
                    id = fileIdProvider(),
                    fileId = fileId,
                    fileName = name,
                    getPlainInputStream = { cryptoRepository.getFileEditionDecryptStream(encFile) },
                    file = encFile,
                    fileExtension = OSMediaType.PHOTO.extension,
                    safeItemFieldKind = SafeItemFieldKind.Photo,
                ),
            )
        } else {
            LBResult.Failure(OSAppError.Code.IMAGE_CAPTURED_NOT_FOUND.get())
        }
    }

    /**
     * Prepare file with right permission for plain image capture via the System Camera app
     */
    private fun prepareDataForFieldCaptureExternal(): ExternalPhotoCapture {
        imageCaptureCacheDir.deleteRecursively()
        val file = File(imageCaptureCacheDir, "captured_photo.${OSMediaType.PHOTO.extension}").also { it.parentFile?.mkdirs() }
        val authority = "${context.packageName}.${AppConstants.FileProvider.FileProviderAuthoritySuffix}"
        val publicUri = FileProvider.getUriForFile(context, authority, file)
        return ExternalPhotoCapture(file, publicUri)
    }

    /**
     * File for image capture via the CameraActivity
     */
    private fun prepareDataForFieldCaptureInApp(): InAppMediaCapture {
        return InAppMediaCapture(
            plainThumbnailFile = File(imageCaptureCacheDir, "thumbnail.${OSMediaType.PHOTO.extension}"),
            encryptedFile = File(imageCaptureCacheDir, "encrypted_file"),
            mediaType = OSMediaType.PHOTO,
        )
    }

    fun prepareDataForFieldImageCapture(): Flow<CameraData> {
        return getAppSettingUseCase.cameraSystemFlow().map { cameraSystem ->
            when (cameraSystem) {
                CameraSystem.InApp -> CameraData.InApp(prepareDataForFieldCaptureInApp())
                CameraSystem.External -> CameraData.External(lazy { prepareDataForFieldCaptureExternal() })
            }
        }
    }

    override fun close() {
        coroutineScope.cancel()
        imageCaptureCacheDir.deleteRecursively()
        encryptedCacheDir.deleteRecursively()
    }
}

@Serializable
data class InAppMediaCapture(
    @Serializable(with = NullableFileSerializer::class)
    val plainThumbnailFile: File?,
    @Serializable(with = NullableFileSerializer::class)
    val encryptedFile: File?,
    val mediaType: OSMediaType,
) {
    fun delete(): Boolean {
        return plainThumbnailFile?.delete() ?: true && encryptedFile?.delete() ?: true
    }
}

@Serializable
data class ExternalPhotoCapture(
    @Serializable(with = FileSerializer::class)
    val file: File,
    @Serializable(with = UriSerializer::class)
    val publicUri: Uri,
) {
    fun delete(): Boolean {
        return file.delete()
    }
}
