package studio.lunabee.onesafe.common.extensions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.core.content.FileProvider
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.OSString
import java.io.File

fun Context.createTempFile(fileName: String, directory: File = getCacheImageFolder()): File {
    directory.mkdirs()
    return File.createTempFile(fileName, null, directory)
}

fun Context.getFileSharingIntent(
    fileToShare: File,
    mimeType: String,
    fileProviderAuthority: String = AppConstants.FileProvider.getAuthority(packageName), // TODO change authority
): Intent {
    val uri = FileProvider.getUriForFile(this@getFileSharingIntent, fileProviderAuthority, fileToShare)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        setDataAndType(uri, mimeType)
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    return Intent.createChooser(sendIntent, null)
}

fun Context.getCacheImageFolder(): File = File(cacheDir, AppConstants.FileProvider.ImageCacheFolderName)
fun Context.hasBiometric(): Boolean =
    BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        BiometricManager.BIOMETRIC_SUCCESS

fun Context.showCopyToast(
    label: String,
) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        Toast.makeText(
            this,
            getString(OSString.common_copy_success, label),
            Toast.LENGTH_SHORT,
        ).show()
    }
}
