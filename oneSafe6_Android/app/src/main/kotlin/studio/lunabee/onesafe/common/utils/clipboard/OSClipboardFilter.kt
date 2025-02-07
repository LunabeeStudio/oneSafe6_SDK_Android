package studio.lunabee.onesafe.common.utils.clipboard

import android.net.Uri
import com.lunabee.lbextensions.remove

enum class OSClipboardFilter(
    val isValid: (text: String) -> Boolean = { true },
    val mapText: (text: String) -> String = { it },
) {
    Url(
        isValid = { android.util.Patterns.WEB_URL.matcher(it).matches() },
        mapText = { Uri.parse(it).host?.trim()?.remove("www.") ?: it },
    ),
    None,
}
