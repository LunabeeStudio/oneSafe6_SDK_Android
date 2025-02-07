package studio.lunabee.onesafe.common.extensions

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

fun <T : Parcelable?> Bundle.getParcelableCompact(
    key: String?,
    clazz: Class<T>,
): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
