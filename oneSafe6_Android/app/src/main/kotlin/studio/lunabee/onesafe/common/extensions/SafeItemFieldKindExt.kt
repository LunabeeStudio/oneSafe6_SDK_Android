package studio.lunabee.onesafe.common.extensions

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind

@Composable
fun SafeItemFieldKind.Unknown.getStringFromUnknownSafeItem(): String {
    return when (id) {
        "file" -> LbcTextSpec.StringResource(OSString.fieldName_file)
        "photo" -> LbcTextSpec.StringResource(OSString.fieldName_photosAndVideos)
        "video" -> LbcTextSpec.StringResource(OSString.fieldName_photosAndVideos)
        else -> LbcTextSpec.Raw(id)
    }.string
}
