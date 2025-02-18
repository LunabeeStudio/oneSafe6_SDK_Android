package studio.lunabee.onesafe.feature.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardContainsSafeDataUseCase
import javax.inject.Inject

class AndroidClipboardContainsSafeDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClipboardContainsSafeDataUseCase {
    override fun invoke(): Boolean? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.primaryClipDescription?.extras?.getBoolean(BuildConfig.APPLICATION_ID) == true
        } else {
            null
        }
    }
}
