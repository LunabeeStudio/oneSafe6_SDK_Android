package studio.lunabee.onesafe.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.home.model.ItemRowData
import studio.lunabee.onesafe.feature.itemactions.OSSafeItemWithAction
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.model.clickableWithHaptic
import studio.lunabee.onesafe.molecule.OSSafeItem
import studio.lunabee.onesafe.molecule.OSSectionHeader
import studio.lunabee.onesafe.molecule.OSShimmerSafeItem
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
object HomeLazyGridItems {
    fun settingSpacer(
        lazyGridScope: LazyGridScope,
    ) {
        lazyGridScope.item(
            key = OSDrawable.ic_settings,
            contentType = ContentTypeSpacer,
            span = MaxLineSpan,
        ) {
            Spacer(
                modifier = Modifier
                    .padding(top = OSDimens.SystemButton.Regular),
            )
        }
    }

    context(LazyGridScope)
    fun sectionHeader(
        text: LbcTextSpec,
        key: Any,
        accessibilityModifier: Modifier = Modifier,
        action: @Composable (RowScope.() -> Unit)? = null,
    ) {
        item(
            key = key,
            contentType = ContentTypeSectionHeader,
            span = MaxLineSpan,
        ) {
            OSSectionHeader(
                text = text,
                actionButton = action,
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
                    .padding(
                        start = OSDimens.SystemSpacing.Regular,
                        // Remove default padding of action button if present
                        end = OSDimens.SystemSpacing.Regular - (OSDimens.AlternativeSpacing.Dimens12.takeIf { action != null } ?: 0.dp),
                        bottom = OSDimens.SystemSpacing.Regular,
                    )
                    .then(other = accessibilityModifier),
            )
        }
    }

    context(LazyGridScope)
    fun sectionSpacer() {
        item(
            contentType = ContentTypeSpacer,
            span = MaxLineSpan,
        ) {
            Spacer(
                modifier = Modifier
                    .padding(top = OSDimens.SystemSpacing.ExtraLarge),
            )
        }
    }

    fun elementsFavorite(
        lazyGridScope: LazyGridScope,
        favoriteRowDataList: ImmutableList<ItemRowData>,
        onItemClick: (UUID) -> Unit,
        onMoreClick: () -> Unit,
        itemStyle: OSSafeItemStyle,
    ) {
        lazyGridScope.itemsRowData(
            rowDataList = favoriteRowDataList,
            onItemClick = onItemClick,
            onMoreClick = onMoreClick,
            RowConfig(
                itemStyle = itemStyle,
                itemPerRow = AppConstants.Ui.HomeFavorite.ItemPerRow,
                emptyText = LbcTextSpec.StringResource(OSString.home_section_favorites_empty),
                moreLabel = LbcTextSpec.StringResource(OSString.home_section_favorites_andAllOther_accessibility),
                moreContentDescription = LbcTextSpec.StringResource(OSString.home_section_favorites_seeAll_accessibility),
                key = KeyFavoriteRow,
            ),
        )
    }

    private fun LazyGridScope.itemsRowData(
        rowDataList: ImmutableList<ItemRowData>,
        onItemClick: (UUID) -> Unit,
        onMoreClick: () -> Unit,
        rowConfig: RowConfig,
    ) {
        if (rowDataList.isEmpty() && rowConfig.emptyText != null) {
            emptyCard(rowConfig.key, text = rowConfig.emptyText)
        } else {
            item(
                key = rowConfig.key,
                span = MaxLineSpan,
                contentType = ContentTypeRowContainer,
            ) {
                val horizontalPadding = OSDimens.SystemSpacing.Regular
                val width = LocalConfiguration.current.screenWidthDp
                val itemHorizontalPadding = OSDimens.SystemSpacing.Small
                val itemFullSize = rowConfig.itemStyle.elementSize.value + 2 * itemHorizontalPadding.value
                val itemSpacing = remember(width) {
                    (
                        (width - horizontalPadding.value - rowConfig.itemPerRow * itemFullSize) / (
                            ceil(
                                rowConfig.itemPerRow,
                            ) - 1
                            )
                        ).dp
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(space = itemSpacing),
                    modifier = Modifier
                        .animateItem()
                        .testTag(UiConstants.TestTag.Item.HomeItemSectionRow)
                        .fillMaxWidth(),
                ) {
                    itemsIndexed(
                        items = rowDataList,
                        key = { idx, rowData ->
                            when (rowData) {
                                is ItemRowData.Item -> rowData.item.id
                                is ItemRowData.More -> OSPlurals.home_section_andAllOther
                                ItemRowData.Loading -> idx
                            }
                        },
                        contentType = { _, rowData ->
                            when (rowData) {
                                ItemRowData.Loading,
                                is ItemRowData.Item,
                                -> ContentTypeRowDataItem + rowConfig.itemStyle
                                is ItemRowData.More -> ContentTypeRowMoreItem + rowConfig.itemStyle
                            }
                        },
                    ) { index, rowData ->
                        val modifier = when (index) {
                            0 -> Modifier.padding(start = horizontalPadding)
                            rowDataList.lastIndex -> Modifier.padding(end = horizontalPadding)
                            else -> Modifier
                        }

                        when (rowData) {
                            is ItemRowData.Item -> {
                                val item = rowData.item
                                val itemName = item.itemNameProvider.name
                                OSSafeItemWithAction(
                                    illustration = item.safeIllustration,
                                    style = rowConfig.itemStyle,
                                    label = itemName,
                                    labelMinLines = ItemRowDataItemLabelMinLines,
                                    clickLabel = LbcTextSpec.StringResource(
                                        id = OSString.accessibility_home_itemClicked,
                                        itemName.string,
                                    ),
                                    onClick = {
                                        onItemClick(item.id)
                                    },
                                    modifier = Modifier.animateItem()
                                        .then(other = modifier)
                                        .clip(shape = MaterialTheme.shapes.medium),
                                    getActions = item.actions,
                                    paddingValues = PaddingValues(all = itemHorizontalPadding),
                                )
                            }
                            is ItemRowData.More -> {
                                OSSafeItem(
                                    illustration = OSItemIllustration.Image(
                                        OSImageSpec.Drawable(
                                            drawable = OSDrawable.ic_menu,
                                            tintColor = MaterialTheme.colorScheme.secondaryContainer,
                                        ),
                                    ),
                                    style = rowConfig.itemStyle,
                                    label = LbcTextSpec.PluralsResource(
                                        id = OSPlurals.home_section_andAllOther,
                                        rowData.moreCount,
                                        rowData.moreCount,
                                    ),
                                    modifier = Modifier
                                        .then(other = modifier)
                                        .clip(shape = MaterialTheme.shapes.medium)
                                        .clickableWithHaptic(
                                            onClick = onMoreClick,
                                            onClickLabel = rowConfig.moreLabel.string,
                                        )
                                        .padding(all = itemHorizontalPadding),
                                    contentDescription = rowConfig.moreContentDescription,
                                )
                            }
                            ItemRowData.Loading -> OSShimmerSafeItem(
                                style = rowConfig.itemStyle,
                                modifier = modifier,
                            )
                        }
                    }
                }
            }
        }
    }

    fun emptyElementCard(lazyGridScope: LazyGridScope) {
        lazyGridScope.emptyCard(
            OSString.home_section_elements_empty,
            LbcTextSpec.StringResource(OSString.home_section_elements_empty),
        )
    }

    private fun LazyGridScope.emptyCard(key: Any, text: LbcTextSpec) {
        item(
            key = key,
            span = MaxLineSpan,
        ) {
            OSMessageCard(
                description = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
        }
    }

    fun bottomSpacer(lazyGridScope: LazyGridScope) {
        lazyGridScope.item(span = MaxLineSpan) {
            Spacer(
                modifier = Modifier
                    .height(OSDimens.SystemSpacing.ExtraSmall),
            )
        }
    }

    fun othersCardItem(
        lazyGridScope: LazyGridScope,
        actions: List<OthersAction>,
        key: Any?,
        modifier: Modifier = Modifier,
    ) {
        lazyGridScope.item(
            contentType = ContentTypeOtherCard,
            span = MaxLineSpan,
            key = key,
        ) {
            OthersCard(actions = actions, modifier = modifier)
        }
    }

    private const val ContentTypeSectionHeader: String = "TypeSectionHeaderContentType"
    private const val ContentTypeSpacer: String = "SpacerContentType"
    private const val ContentTypeRowContainer: String = "ContentTypeRowContainer"
    private const val ContentTypeRowDataItem: String = "ContentTypeRowDataItem"
    private const val ContentTypeRowMoreItem: String = "ContentTypeRowMoreItem"
    private const val ContentTypeOtherCard = "OthersCard"

    private const val KeyFavoriteRow: String = "KeyFavoriteRow"

    private val MaxLineSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(currentLineSpan = maxLineSpan) }
    private const val ItemRowDataItemLabelMinLines: Int = 2

    private class RowConfig(
        val itemStyle: OSSafeItemStyle,
        val itemPerRow: Float,
        val emptyText: LbcTextSpec?,
        val moreLabel: LbcTextSpec,
        val moreContentDescription: LbcTextSpec,
        val key: Any,
    )
}
