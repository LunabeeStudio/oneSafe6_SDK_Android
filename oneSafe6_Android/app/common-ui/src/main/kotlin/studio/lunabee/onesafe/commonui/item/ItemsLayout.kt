package studio.lunabee.onesafe.commonui.item

import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout

enum class ItemsLayout {
    Grid,
    List,
    ;

    companion object {
        fun from(itemLayouts: ItemLayout): ItemsLayout = when (itemLayouts) {
            ItemLayout.Grid,
            ItemLayout.LargeGrid,
            -> Grid
            ItemLayout.List -> List
        }
    }
}
