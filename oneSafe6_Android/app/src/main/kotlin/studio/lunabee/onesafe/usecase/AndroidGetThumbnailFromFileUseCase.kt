package studio.lunabee.onesafe.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.core.graphics.toRectF
import androidx.exifinterface.media.ExifInterface
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.extensions.getRotationInDegrees
import studio.lunabee.onesafe.common.model.FileThumbnailData
import studio.lunabee.onesafe.commonui.extension.getMimeType
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = LBLogger.get<AndroidGetThumbnailFromFileUseCase>()

class AndroidGetThumbnailFromFileUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(file: File, isFullWidth: Boolean = false, displayDuration: Boolean = true): FileThumbnailData {
        return withContext(fileDispatcher) {
            val mimeType = Uri.fromFile(file).getMimeType(context)
            val size: Size = if (isFullWidth) {
                context.resources.displayMetrics.widthPixels.let { Size(it, it) }
            } else {
                Size(thumbnailWidth, thumbnailHeight)
            }
            try {
                when {
                    mimeType == null -> FileThumbnailData.FileThumbnailPlaceholder.File
                    mimeType.contains(AppConstants.FileProvider.ImageMimeTypePrefix) -> {
                        getThumbnailBitmapFromImageFile(file, size)?.let {
                            FileThumbnailData.Data(OSImageSpec.Bitmap(it))
                        } ?: FileThumbnailData.FileThumbnailPlaceholder.Image
                    }
                    mimeType.contains(AppConstants.FileProvider.VideoMimeTypePrefix) -> {
                        getThumbnailForVideo(file, size, displayDuration)
                    }
                    mimeType.contains(AppConstants.FileProvider.AudioMimeTypePrefix) -> {
                        getThumbnailBitmapFromAudioFile(file, size)?.let {
                            FileThumbnailData.Data(OSImageSpec.Bitmap(it))
                        } ?: FileThumbnailData.FileThumbnailPlaceholder.Music
                    }
                    mimeType.contains(AppConstants.FileProvider.PdfExtension) -> {
                        getThumbnailBitmapFromPdfFile(file, size)?.let {
                            FileThumbnailData.Data(OSImageSpec.Bitmap(it))
                        } ?: FileThumbnailData.FileThumbnailPlaceholder.File
                    }
                    else -> FileThumbnailData.FileThumbnailPlaceholder.File
                }
            } catch (e: FileNotFoundException) {
                FileThumbnailData.FileThumbnailPlaceholder.File
            } catch (e: NullPointerException) {
                FileThumbnailData.FileThumbnailPlaceholder.File
            }
        }
    }

    /**
     * Shortcut to get a thumbnail for a video if we already know the mimetype (for example in video capture flow)
     */
    suspend fun video(file: File, isFullWidth: Boolean = false, displayDuration: Boolean = true): FileThumbnailData {
        return withContext(fileDispatcher) {
            val size: Size = if (isFullWidth) {
                context.resources.displayMetrics.widthPixels.let { Size(it, it) }
            } else {
                Size(thumbnailWidth, thumbnailHeight)
            }
            try {
                getThumbnailForVideo(file, size, displayDuration)
            } catch (e: FileNotFoundException) {
                FileThumbnailData.FileThumbnailPlaceholder.File
            } catch (e: NullPointerException) {
                FileThumbnailData.FileThumbnailPlaceholder.File
            }
        }
    }

    private fun getThumbnailForVideo(
        file: File,
        size: Size,
        displayDuration: Boolean,
    ) = getThumbnailBitmapFromVideoFile(file, size, displayDuration)?.let {
        FileThumbnailData.Data(OSImageSpec.Bitmap(it))
    } ?: FileThumbnailData.FileThumbnailPlaceholder.Video

    /**
     * Take an image file a generate a thumbnail taking EXIF orientation into account
     */
    private fun getThumbnailBitmapFromImageFile(
        file: File,
        size: Size,
    ): Bitmap? {
        return try {
            val exif = ExifInterface(file.path)
            val rotationInDegrees = exif.getRotationInDegrees()
            val matrix = Matrix()
            val thumbnail = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(file.path),
                size.width,
                size.height,
            )
            return if (rotationInDegrees != 0f) {
                matrix.preRotate(rotationInDegrees)
                Bitmap.createBitmap(
                    thumbnail,
                    0,
                    0,
                    size.width,
                    size.height,
                    matrix,
                    false,
                )
            } else {
                thumbnail
            }
        } catch (e: NullPointerException) {
            null
        } catch (e: FileNotFoundException) {
            null
        }
    }

    private fun getThumbnailBitmapFromVideoFile(
        file: File,
        size: Size,
        displayDuration: Boolean,
    ): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val videoThumbnail = ThumbnailUtils.createVideoThumbnail(file, size, null)
                // Create a Squared image from video thumbnail
                val croppedThumbnail = ThumbnailUtils.extractThumbnail(videoThumbnail, size.width, size.height)
                if (displayDuration) {
                    val duration = MediaMetadataRetriever().use { retriever ->
                        retriever.setDataSource(file.path)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                    }
                    addDurationTextToThumbnail(croppedThumbnail, duration.milliseconds)
                } else {
                    croppedThumbnail
                }
            } catch (e: IOException) {
                logger.e(e)
                null
            } catch (e: IllegalArgumentException) {
                null
            }
        } else {
            null
        }
    }

    private fun addDurationTextToThumbnail(
        imageBitmap: Bitmap,
        duration: Duration,
    ): Bitmap {
        val text = String.format(Locale.getDefault(), durationFormat, duration.inWholeMinutes, duration.inWholeSeconds % 60)
        val bitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // If we are drawing a big image, we take 24.dp as text size, otherwise, we take a ratio of the image
        val textSize = min(bitmap.height / textSizeDurationRatio, textSizeDp * context.resources.displayMetrics.density)

        // Create proportional margin
        val paddingHorizontal = (textSize / 2).toInt()
        val paddingVertical = (textSize / 4).toInt()
        val margin = (textSize / 4).toInt()

        val canvas = Canvas(bitmap)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.BLACK
        textPaint.textSize = textSize

        // Calculate the surface of the text
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)

        // Apply padding to rect
        bounds.inset(-paddingHorizontal, -paddingVertical)

        val x: Int = paddingHorizontal + margin
        val y: Int = bitmap.height - paddingVertical - margin

        // Apply the good offset to the rect
        val rectF = bounds.toRectF().apply { offset(x.toFloat(), y.toFloat()) }
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint.color = Color.WHITE

        // Draw the rect around the text
        canvas.drawRoundRect(rectF, rectF.height() / 2, rectF.height() / 2, rectPaint)

        // Draw the text
        canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
        return bitmap
    }

    private fun getThumbnailBitmapFromAudioFile(
        file: File,
        size: Size,
    ): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ThumbnailUtils.createAudioThumbnail(file, size, null)
            } catch (e: IOException) {
                logger.e(e)
                null
            }
        } else {
            null
        }
    }

    private fun getThumbnailBitmapFromPdfFile(
        file: File,
        size: Size,
    ): Bitmap? {
        return try {
            context.contentResolver.openFileDescriptor(Uri.fromFile(file), "r")?.use { parcelFileDescriptor ->
                PdfRenderer(parcelFileDescriptor).openPage(0).use { pdfRenderer ->
                    val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    pdfRenderer.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            // PDF is secured
            null
        }
    }

    companion object {
        const val thumbnailHeight: Int = AppConstants.Ui.Item.ThumbnailFilePixelSize
        const val thumbnailWidth: Int = AppConstants.Ui.Item.ThumbnailFilePixelSize

        const val textSizeDurationRatio: Float = 7.5f
        const val textSizeDp: Float = 18f
        const val durationFormat: String = "%02d:%02d"
    }
}
