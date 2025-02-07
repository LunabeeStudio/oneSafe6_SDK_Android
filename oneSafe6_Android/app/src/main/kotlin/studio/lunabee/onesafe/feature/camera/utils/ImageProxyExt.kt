package studio.lunabee.onesafe.feature.camera.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.exifinterface.media.ExifInterface
import studio.lunabee.onesafe.common.extensions.getRotationInDegrees
import studio.lunabee.onesafe.domain.utils.ByteBufferBackedInputStream

fun ImageProxy.toOrientedBitmap(): Bitmap {
    val exif = getExif()
    val rotationInDegrees = exif.getRotationInDegrees()
    val bitmap = this.toBitmap()
    val matrix = Matrix()
    return if (rotationInDegrees != 0f) {
        matrix.preRotate(rotationInDegrees)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            false,
        )
    } else {
        bitmap
    }
}

private fun ImageProxy.getExif(): ExifInterface {
    val byteBuffer = planes[0].buffer
    return ByteBufferBackedInputStream(byteBuffer).use(::ExifInterface)
}
