package studio.lunabee.onesafe.commonui.extension

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.Locale

fun Uri.getMimeType(context: Context): String? {
    return if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        val contentResolver: ContentResolver = context.contentResolver
        contentResolver.getType(this)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
    }
}
