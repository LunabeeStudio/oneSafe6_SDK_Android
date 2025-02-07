package studio.lunabee.onesafe.common.model.item

import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSItemIllustration
import java.util.UUID

sealed interface PlainItemData {
    val id: UUID
    val itemNameProvider: OSNameProvider
    val icon: ByteArray?
    val color: Color?
    val safeIllustration: OSItemIllustration
    val actions: (suspend () -> List<SafeItemAction>)?
}
