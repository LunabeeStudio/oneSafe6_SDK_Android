package studio.lunabee.onesafe.feature.fileviewer.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import java.io.File

sealed interface FileViewerUiState {

    data class Data(
        val viewerType: ViewerType,
        val file: File,
        val mimeType: String,
        val name: String,
    ) : FileViewerUiState

    data class Loading(
        val name: String? = null,
    ) : FileViewerUiState

    data class Error(
        val error: LbcTextSpec,
        val name: String?,
    ) : FileViewerUiState
}

enum class ViewerType {
    Pdf, Photo, Video, Audio, Unknown;

    companion object {
        fun fromField(mimeType: String?): ViewerType {
            return when {
                mimeType == null -> Unknown
                mimeType.startsWith(AppConstants.FileProvider.ImageMimeTypePrefix) -> Photo
                mimeType.startsWith(AppConstants.FileProvider.VideoMimeTypePrefix) -> Video
                mimeType.startsWith(AppConstants.FileProvider.AudioMimeTypePrefix) -> Audio
                mimeType.endsWith(AppConstants.FileProvider.PdfExtension) -> Pdf
                else -> Unknown
            }
        }
    }
}
