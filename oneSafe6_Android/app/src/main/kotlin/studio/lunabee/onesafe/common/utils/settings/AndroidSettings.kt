package studio.lunabee.onesafe.common.utils.settings

import android.content.Context
import android.net.Uri
import android.provider.Settings

/**
 * Ref :
 * @see <a href="https://github.com/florisboard/florisboard" />
 */
object AndroidSettings {

    val Secure: AndroidSettingsHelper = object : AndroidSettingsHelper() {
        override fun getString(context: Context, key: String): String? {
            return runCatching { Settings.Secure.getString(context.contentResolver, key) }.getOrNull()
        }

        override fun getUriFor(key: String): Uri? {
            return runCatching { Settings.Secure.getUriFor(key) }.getOrNull()
        }
    }
}
