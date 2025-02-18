package studio.lunabee.onesafe.common.utils

import android.content.ClipDescription
import android.os.Build

object ClipDescriptionCompat {
    val extraIsSensitive: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            "android.content.extra.IS_SENSITIVE"
        }
}
