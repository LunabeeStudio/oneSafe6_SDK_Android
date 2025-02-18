package studio.lunabee.onesafe.commonui.localprovider

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.model.OSSafeItemStyle

val LocalItemStyle: ProvidableCompositionLocal<ItemStyleHolder> = compositionLocalOf {
    ItemStyleHolder(OSSafeItemStyle.Regular, OSSafeItemStyle.Large, ItemsLayout.Grid)
}
