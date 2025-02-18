package studio.lunabee.onesafe.common.extensions

import androidx.exifinterface.media.ExifInterface

fun ExifInterface.getRotationInDegrees(): Float {
    val rotation = getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
    )
    return exifToDegrees(rotation)
}

private fun exifToDegrees(exifOrientation: Int): Float {
    return when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
}
