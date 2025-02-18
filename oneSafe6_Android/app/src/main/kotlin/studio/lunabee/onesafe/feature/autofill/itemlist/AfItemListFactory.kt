package studio.lunabee.onesafe.feature.autofill.itemlist

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSLazyCard
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.search.composable.SearchItem
import studio.lunabee.onesafe.feature.search.composable.cardcontent.SearchItemCardContent
import studio.lunabee.onesafe.feature.search.composable.cardcontent.TitleCardContent
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSShimmerItemRow
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

object AfItemListFactory {

    context(LazyListScope)
    fun addSafeItems(
        safeItems: List<PlainItemDataRow>,
        onClickOnItem: (UUID) -> Unit,
        header: LbcTextSpec,
        headerKey: String,
    ) {
        val cardContents = mutableListOf<OSLazyCardContent>()

        cardContents += TitleCardContent(
            title = header,
            key = headerKey,
        )

        cardContents += safeItems.map {
            SearchItemCardContent(
                osSafeItemData = it,
                key = it.id,
                onClick = { onClickOnItem(it.id) },
            )
        }
        osLazyCard(cardContents)
    }

    context(LazyListScope)
    fun addPaginatedSafeItems(
        safeItems: LazyPagingItems<PlainItemDataRow>,
        onClickOnItem: (UUID) -> Unit,
        showHeader: Boolean,
    ) {
        val cardContents = mutableListOf<OSLazyCardContent>()

        if (showHeader) {
            cardContents += TitleCardContent(
                title = LbcTextSpec.StringResource(OSString.autofill_safeItemsList_otherSectionTitle),
                key = OtherHeaderKey,
            )
        }

        cardContents += OSLazyCardContent.Paged { positionInList ->
            paginatedItem(safeItems, onClickOnItem, positionInList)
        }
        osLazyCard(cardContents)
    }

    context(LazyListScope)
    private fun paginatedItem(
        safeItems: LazyPagingItems<PlainItemDataRow>,
        onClickOnItem: (UUID) -> Unit,
        positionInList: OSLazyCardContent.Position,
    ) {
        items(
            count = safeItems.itemCount,
            key = { (safeItems.peek(it)?.id) ?: it },
            contentType = { SearchItemContentType },
        ) { index ->
            safeItems[index]?.let { item ->
                val position = OSLazyCardContent.Position.fromIndexPaged(
                    idx = index,
                    lastIndex = safeItems.itemCount - 1,
                    positionInParent = positionInList,
                )

                OSLazyCard(position) {
                    SearchItem(
                        osItemIllustration = item.safeIllustration,
                        label = item.itemNameProvider.name,
                        paddingValues = PaddingValues(vertical = OSDimens.SystemSpacing.Small),
                        onClick = { onClickOnItem(item.id) },
                        identifier = item.identifier,
                    )
                }
            }
        }
    }

    fun addShimmersItems(
        lazyListScope: LazyListScope,
    ) {
        val cardContents = mutableListOf<OSLazyCardContent>()
        repeat(ShimmeringItemCount) {
            cardContents += object : OSLazyCardContent.Item {
                override val key: Int = it
                override val contentType: String = ShimmeringItemContentType

                @Composable
                override fun Content(padding: PaddingValues, modifier: Modifier) {
                    OSShimmerItemRow(
                        paddingValues = PaddingValues(vertical = OSDimens.SystemSpacing.Small),
                    )
                }
            }
        }
        lazyListScope.apply {
            osLazyCard(cardContents)
        }
    }

    const val SuggestionHeaderKey: String = "SuggestionHeaderKey"
    private const val OtherHeaderKey: String = "OtherHeaderKey"
    private const val SearchItemContentType: String = "SearchItemContentType"
    private const val ShimmeringItemContentType: String = "ShimmeringItemContentType"
    private const val ShimmeringItemCount: Int = 15
}
