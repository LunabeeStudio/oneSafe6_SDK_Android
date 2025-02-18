package studio.lunabee.onesafe.commonui.extension

import android.content.Intent

// FIXME https://github.com/android/android-test/issues/1939

fun Intent.setIsTest(): Intent = this.putExtra("IS_TEST", true)

val Intent.isTest: Boolean
    get() = this.getBooleanExtra("IS_TEST", false)

// Fix security exception when trying to read unexpected Uri like Calendar intents
val Intent.hasUriReadPermission: Boolean
    get() = flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 1
