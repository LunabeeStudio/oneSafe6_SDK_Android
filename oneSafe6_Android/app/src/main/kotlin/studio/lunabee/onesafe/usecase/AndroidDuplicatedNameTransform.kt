package studio.lunabee.onesafe.usecase

import android.content.Context
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.common.DuplicatedNameTransform

class AndroidDuplicatedNameTransform(private val context: Context) : DuplicatedNameTransform {
    override fun invoke(originalName: String?): String {
        return context.getString(OSString.safeItem_defaultDuplicatedName, originalName)
    }
}
