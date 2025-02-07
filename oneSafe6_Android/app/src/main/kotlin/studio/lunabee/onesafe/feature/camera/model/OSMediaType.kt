package studio.lunabee.onesafe.feature.camera.model

import androidx.annotation.StringRes
import studio.lunabee.onesafe.commonui.OSString

enum class OSMediaType(
    val extension: String,
    @StringRes val stringNameRes: Int,
) {
    PHOTO(extension = "jpeg", stringNameRes = OSString.fieldName_photo_count),
    VIDEO(extension = "mp4", stringNameRes = OSString.fieldName_video_count),
}
