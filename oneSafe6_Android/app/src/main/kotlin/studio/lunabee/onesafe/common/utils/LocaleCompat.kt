package studio.lunabee.onesafe.common.utils

import android.content.Context

object LocaleCompat {
    fun getMainLocale(context: Context): String = context.resources.configuration.locales.get(0).language
}
