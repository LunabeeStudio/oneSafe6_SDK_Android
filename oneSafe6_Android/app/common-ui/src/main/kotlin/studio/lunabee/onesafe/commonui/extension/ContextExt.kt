package studio.lunabee.onesafe.commonui.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.biometric.BiometricHardware

/**
 * Find the closest Activity in a given Context.
 * https://github.com/google/accompanist/blob/8ce3ce475d6888961fa1bd93986199928f3c8b93/permissions/src/main/java/com/google/accompanist/permissions/PermissionsUtil.kt#L129-L139
 */
fun Context.findFragmentActivity(): FragmentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    error("Couldn't find any activity")
}

fun Context.biometricHardware(): BiometricHardware =
    when {
        packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) -> BiometricHardware.FEATURE_FINGERPRINT
        packageManager.hasSystemFeature(PackageManager.FEATURE_IRIS) -> BiometricHardware.FEATURE_IRIS
        packageManager.hasSystemFeature(PackageManager.FEATURE_FACE) -> BiometricHardware.FEATURE_FACE
        else -> BiometricHardware.NONE
    }

fun Context.getTextSharingIntent(
    textToShare: String,
): Intent {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, textToShare)
    }
    return Intent.createChooser(sendIntent, null)
}

fun Context.copyToClipBoard(string: String, label: LbcTextSpec) {
    val clipMan: ClipboardManager? = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(label.string(this), string)
    clipMan?.setPrimaryClip(clip)
}
