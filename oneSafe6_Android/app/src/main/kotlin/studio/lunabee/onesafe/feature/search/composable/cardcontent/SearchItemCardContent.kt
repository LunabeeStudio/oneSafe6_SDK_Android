package studio.lunabee.onesafe.feature.search.composable.cardcontent

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.feature.itemactions.OSItemRowWithAction
import studio.lunabee.onesafe.model.OSLazyCardContent

class SearchItemCardContent(
    private val osSafeItemData: PlainItemDataRow,
    private val onClick: () -> Unit,
    override val key: Any = osSafeItemData.id,
) : OSLazyCardContent.Item {
    override val contentType: Any = "SearchItem"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        OSItemRowWithAction(
            osItemIllustration = osSafeItemData.safeIllustration,
            label = osSafeItemData.itemNameProvider.name,
            modifier = modifier,
            onClick = onClick,
            paddingValues = padding,
            subtitle = osSafeItemData.identifier,
            getActions = osSafeItemData.actions,
        )
    }
}
