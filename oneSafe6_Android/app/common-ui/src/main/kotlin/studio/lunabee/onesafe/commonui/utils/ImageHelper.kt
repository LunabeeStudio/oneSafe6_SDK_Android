package studio.lunabee.onesafe.commonui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import java.io.ByteArrayOutputStream
import javax.inject.Inject

private const val EmojiTextSize: Float = 290.0f

class ImageHelper @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) {
    suspend fun byteArrayToBitmap(bytes: ByteArray): Bitmap? = withContext(dispatcher) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    suspend fun osImageDataToBitmap(context: Context, image: OSImageSpec): Bitmap? = when (val data = image.data) {
        is Uri -> withContext(fileDispatcher) { context.contentResolver.openInputStream(data)?.use { BitmapFactory.decodeStream(it) } }
        is ByteArray -> withContext(dispatcher) { byteArrayToBitmap(bytes = data) }
        is Bitmap -> image.data as Bitmap
        is Int -> BitmapFactory.decodeResource(
            context.resources,
            image.data as Int,
        )
        else -> error("Data type is not handled")
    }

    suspend fun extractColorPaletteFromBitmap(bitmap: Bitmap): Palette = withContext(dispatcher) {
        Palette.from(bitmap).generate() // synchronously generated
    }

    suspend fun createBitmapWithText(text: String): Bitmap? = withContext(dispatcher) {
        val paint = Paint()
        paint.textSize = EmojiTextSize
        val textRect = Rect()
        paint.getTextBounds(text, 0, text.length, textRect)
        if (textRect.isEmpty) {
            null
        } else {
            val bitmap = Bitmap.createBitmap(textRect.width(), textRect.height(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawText(text, 0f, textRect.height().toFloat() - paint.fontMetrics.bottom, paint)
            bitmap
        }
    }

    suspend fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray = withContext(dispatcher) {
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            stream.toByteArray()
        }
    }

    private fun getBitmapFromDrawable(@DrawableRes iconRes: Int, context: Context): Bitmap? {
        return ContextCompat.getDrawable(context, iconRes)?.let { drawable ->
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            bitmap
        }
    }

    suspend fun convertImageSpecToByteArray(imageSpec: OSImageSpec?, context: Context): ByteArray? = imageSpec?.data?.let { iconData ->
        when (iconData) {
            is ByteArray -> iconData
            is Uri -> withContext(fileDispatcher) { context.contentResolver.openInputStream(iconData)?.use { it.readBytes() } }
            is Bitmap -> withContext(dispatcher) { convertBitmapToByteArray(iconData) }
            is Int -> withContext(dispatcher) {
                getBitmapFromDrawable(iconData, context)?.let {
                    convertBitmapToByteArray(it)
                }
            }
            else -> error("Icon format not supported")
        }
    }
}
