package studio.lunabee.onesafe.feature.search.screen

import androidx.compose.foundation.lazy.LazyListScope
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.feature.search.composable.cardcontent.SearchItemCardContent
import studio.lunabee.onesafe.feature.search.composable.cardcontent.TitleCardContent
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.organism.card.OSMessageCard
import java.util.UUID

context(LazyListScope)
fun searchResultScreen(
    itemCount: Int,
    state: SearchResultUiState.Searching,
    onItemClick: (UUID) -> Unit,
) {
    if (state.result.isNullOrEmpty() && state.deletedResult.isNullOrEmpty()) {
        item {
            if (itemCount == 0) {
                OSMessageCard(
                    description = LbcTextSpec.StringResource(OSString.searchScreen_result_noItem),
                )
            } else {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.searchScreen_result_noResult_title),
                    description = LbcTextSpec.StringResource(OSString.searchScreen_result_noResult_description),
                )
            }
        }
    } else {
        state.result?.let {
            resultCell(
                title = LbcTextSpec.StringResource(OSString.searchScreen_result_title, it.size),
                items = it,
                onItemClick = onItemClick,
            )
        }
        state.deletedResult?.let {
            resultCell(
                title = LbcTextSpec.StringResource(OSString.searchScreen_result_bin_title, it.size),
                items = it,
                onItemClick = onItemClick,
            )
        }
    }
}

context(LazyListScope)
private fun resultCell(
    title: LbcTextSpec,
    items: List<PlainItemDataRow>,
    onItemClick: (UUID) -> Unit,
) {
    val searchCardContents = mutableListOf<OSLazyCardContent>()
    searchCardContents += TitleCardContent(
        title = title,
        key = null,
    )
    searchCardContents += items.map {
        SearchItemCardContent(
            osSafeItemData = it,
            key = it.id,
            onClick = {
                onItemClick(it.id)
            },
        )
    }
    osLazyCard(searchCardContents)
    lazyVerticalOSRegularSpacer()
}
