package studio.lunabee.onesafe.feature.move.selectdestination

import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.model.OSItemIllustration

data class MoveCurrentDestination(
    val itemNameProvider: OSNameProvider,
    val itemIcon: OSItemIllustration,
) {

    // Generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MoveCurrentDestination
        if (itemNameProvider != other.itemNameProvider) return false
        if (itemIcon != other.itemIcon) return false
        return true
    }

    override fun hashCode(): Int {
        var result = itemNameProvider.hashCode()
        result = 31 * result + itemIcon.hashCode()
        return result
    }
}
