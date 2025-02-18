package studio.lunabee.onesafe.feature.search.composable.cardcontent

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsChildrenRow
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsElementLayout
import studio.lunabee.onesafe.model.OSLazyCardContent
import java.util.UUID

class RecentItemCardContent(
    private val elementLayout: ItemDetailsElementLayout,
    private val items: List<PlainItemData>,
    private val onItemClick: (UUID) -> Unit,
    private val indexRow: Int,
    override val key: Any,
) : OSLazyCardContent.Item {
    override val contentType: Any = "consultedItem"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        val sublist = items.takeAt(indexRow * elementLayout.childPerRow, elementLayout.childPerRow)
        ItemDetailsChildrenRow(
            modifier = Modifier.padding(padding),
            children = sublist,
            onItemClick = onItemClick,
            spacing = elementLayout.childSpacing,
            elementStyle = elementLayout.childStyle,
        )
    }
}

private fun <T : Any> List<T>.takeAt(index: Int, n: Int): List<T?> {
    val subList = mutableListOf<T?>()
    var idx = index

    while (size > idx && idx < index + n) {
        subList += get(idx)
        idx++
    }

    return subList
}
