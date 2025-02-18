package studio.lunabee.onesafe.feature.home.model

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.common.model.item.PlainItemData

@Stable
sealed interface ItemRowData {
    class Item(val item: PlainItemData) : ItemRowData
    class More(val moreCount: Int) : ItemRowData
    object Loading : ItemRowData
}
