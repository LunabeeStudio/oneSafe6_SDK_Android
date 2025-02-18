package studio.lunabee.onesafe.feature.clipboard

import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import javax.inject.Inject

interface ClipboardDelegate {
    fun copyText(label: String, value: String, isSecured: Boolean)
}

class ClipboardDelegateImpl @Inject constructor(
    private val clipboardCopyTextUseCase: ClipboardCopyTextUseCase,
) : ClipboardDelegate {
    override fun copyText(label: String, value: String, isSecured: Boolean) {
        clipboardCopyTextUseCase(label, value, isSecured)
    }
}
