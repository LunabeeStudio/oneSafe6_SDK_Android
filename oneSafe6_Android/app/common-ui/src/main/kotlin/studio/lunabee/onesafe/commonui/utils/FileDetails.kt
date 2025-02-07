package studio.lunabee.onesafe.commonui.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import studio.lunabee.onesafe.commonui.utils.FileDetails.Companion.fromUri
import studio.lunabee.onesafe.domain.utils.FileHelper.extension

/**
 * Contains details about a file extracted from a source that does not provides directly those information.
 * For example, URI selected from a content provider can not be map as a [java.io.File] directly, so we have to use [fromUri]
 * to get the name and the extension of the selected file in the system.
 */
data class FileDetails(
    val name: String,
    val size: Long,
    val extension: String?,
) {
    companion object {
        /**
         * Construct a [FileDetails] from internal file or external content provider
         * @see <a href="https://developer.android.com/training/secure-file-sharing/retrieve-info#RetrieveFileInfo">RetrieveFileInfo</a>
         */
        fun fromUri(uri: Uri, context: Context): FileDetails? {
            val localFile = try {
                uri.toFile().takeIf { it.exists() }
            } catch (_: Exception) {
                null
            }

            return if (localFile != null) {
                FileDetails(
                    name = localFile.nameWithoutExtension,
                    size = localFile.length(),
                    extension = localFile.extension,
                )
            } else {
                val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
                context.contentResolver.query(uri, projection, null, null, null)
                    .use { cursor ->
                        cursor?.let {
                            cursor.moveToFirst()
                            val fileName = cursor.getString(0)
                            val size = cursor.getLong(1)
                            FileDetails(
                                name = fileName,
                                size = size,
                                extension = fileName.extension(),
                            )
                        }
                    }
            }
        }
    }
}
