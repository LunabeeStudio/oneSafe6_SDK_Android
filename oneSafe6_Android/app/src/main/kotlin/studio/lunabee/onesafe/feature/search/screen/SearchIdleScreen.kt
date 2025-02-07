package studio.lunabee.onesafe.feature.search.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.atom.osLazyCard
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.itemdetails.computeChildPerRow
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsElementLayout
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.feature.search.composable.cardcontent.RecentItemCardContent
import studio.lunabee.onesafe.feature.search.composable.cardcontent.RecentSearchCardContent
import studio.lunabee.onesafe.feature.search.composable.cardcontent.TitleCardContent
import studio.lunabee.onesafe.feature.search.holder.SearchData
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID
import kotlin.math.ceil
import kotlin.random.Random

context(LazyListScope)
fun searchIdleScreen(
    searchData: SearchData,
    onItemClick: (UUID) -> Unit,
    onSearchClick: (String) -> Unit,
    elementLayout: ItemDetailsElementLayout,
) {
    if (searchData.recentSearch.isEmpty() && searchData.recentItem.isEmpty()) {
        item {
            OSTopImageBox(imageRes = OSDrawable.character_jamy_cool) {
                OSMessageCard(
                    description = LbcTextSpec.StringResource(OSString.searchScreen_recent_emptyText),
                )
            }
        }
    } else {
        if (searchData.recentItem.isNotEmpty()) {
            val itemsCardContents = mutableListOf<OSLazyCardContent>()
            itemsCardContents += TitleCardContent(
                title = LbcTextSpec.StringResource(OSString.searchScreen_recent_items_title),
                key = null,
            )
            val items = searchData.recentItem
            val count = ceil(items.size / elementLayout.childPerRow.toFloat()).toInt()
            repeat(count) {
                itemsCardContents += RecentItemCardContent(
                    items = items,
                    indexRow = it,
                    elementLayout = elementLayout,
                    key = "consultedItem $it",
                    onItemClick = onItemClick,
                )
            }
            osLazyCard(itemsCardContents)
            lazyVerticalOSRegularSpacer()
        }
        if (searchData.recentSearch.isNotEmpty()) {
            val recentSearchCardContent = mutableListOf<OSLazyCardContent>()
            recentSearchCardContent += TitleCardContent(
                title = LbcTextSpec.StringResource(OSString.searchScreen_recent_search_title),
                key = null,
            )
            recentSearchCardContent += searchData.recentSearch.map {
                RecentSearchCardContent(
                    value = LbcTextSpec.Raw(it),
                    onClick = {
                        onSearchClick(it)
                    },
                    key = it,
                )
            }
            osLazyCard(recentSearchCardContent)
            lazyVerticalOSRegularSpacer()
        }
    }
}

@OsDefaultPreview
@Composable
private fun FilledSearchIdleScreenPreview() {
    OSPreviewBackgroundTheme {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val elementLayout = computeChildPerRow(screenWidth, OSSafeItemStyle.Regular)
        LazyColumn(Modifier.fillMaxSize()) {
            searchIdleScreen(
                searchData = SearchData(
                    List<PlainItemData>(5) {
                        PlainItemDataDefault(
                            id = UUID.randomUUID(),
                            itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                            icon = iconSample.takeIf { Random.nextBoolean() },
                            color = randomColor,
                            actions = { listOf(SafeItemAction.AddToFavorites({})) },
                        )
                    },
                    listOf(
                        loremIpsum(2),
                        loremIpsum(4),
                    ),
                ),
                onItemClick = {},
                onSearchClick = {},
                elementLayout = elementLayout,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun EmptySearchIdleScreenPreview() {
    OSPreviewBackgroundTheme {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val elementLayout = computeChildPerRow(screenWidth, OSSafeItemStyle.Regular)
        LazyColumn(Modifier.fillMaxSize()) {
            searchIdleScreen(
                searchData = SearchData(),
                onItemClick = {},
                onSearchClick = {},
                elementLayout = elementLayout,
            )
        }
    }
}
