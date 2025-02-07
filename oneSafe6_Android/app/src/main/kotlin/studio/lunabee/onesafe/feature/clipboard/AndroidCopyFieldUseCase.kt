package studio.lunabee.onesafe.feature.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.common.utils.ClipDescriptionCompat
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardScheduleClearUseCase
import javax.inject.Inject

class AndroidCopyFieldUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardRepository: ClipboardRepository,
    private val clipboardScheduleClearUseCase: ClipboardScheduleClearUseCase,
) : ClipboardCopyTextUseCase {
    override operator fun invoke(label: String, value: String, isSecured: Boolean) {
        // Cancel current cleaner if running
        clipboardScheduleClearUseCase.cancel()

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText(label, value)
        clipData.description?.extras = PersistableBundle().apply {
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#SensitiveContent
            if (isSecured) {
                putBoolean(ClipDescriptionCompat.extraIsSensitive, true)
            }
            putBoolean(BuildConfig.APPLICATION_ID, true)
        }

        clipboardRepository.hasCopiedValue = true
        clipboardManager.setPrimaryClip(clipData)
    }
}
