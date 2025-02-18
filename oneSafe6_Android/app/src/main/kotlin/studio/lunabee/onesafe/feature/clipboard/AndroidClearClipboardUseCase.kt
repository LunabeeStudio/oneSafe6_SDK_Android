package studio.lunabee.onesafe.feature.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardShouldClearUseCase
import javax.inject.Inject

private val logger = LBLogger.get<AndroidClearClipboardUseCase>()

class AndroidClearClipboardUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardShouldClearUseCase: ClipboardShouldClearUseCase,
    private val clipboardRepository: ClipboardRepository,
) : ClipboardClearUseCase {
    override suspend fun invoke(safeId: SafeId) {
        if (clipboardShouldClearUseCase(safeId) != null) {
            clipboardRepository.hasCopiedValue = false
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip()
            } else {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        BuildConfig.APPLICATION_ID,
                        context.getString(OSString.clipboard_overrideText),
                    ),
                )
            }
            logger.v("Clipboard cleared")
        }
    }
}
