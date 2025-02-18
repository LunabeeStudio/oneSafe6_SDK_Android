package studio.lunabee.onesafe.commonui.settings

import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.model.OSSafeItemStyle

class ItemStyleHolder(
    val standardStyle: OSSafeItemStyle,
    val homeFavoriteStyle: OSSafeItemStyle,
    val layout: ItemsLayout,
) {
    companion object {
        fun from(itemLayouts: ItemLayout): ItemStyleHolder {
            val itemsLayout = ItemsLayout.from(itemLayouts)
            return when (itemLayouts) {
                ItemLayout.List -> ItemStyleHolder(OSSafeItemStyle.Regular, OSSafeItemStyle.Large, itemsLayout)
                ItemLayout.Grid -> ItemStyleHolder(OSSafeItemStyle.Regular, OSSafeItemStyle.Large, itemsLayout)
                ItemLayout.LargeGrid -> ItemStyleHolder(OSSafeItemStyle.Large, OSSafeItemStyle.ExtraLarge, itemsLayout)
            }
        }
    }
}
