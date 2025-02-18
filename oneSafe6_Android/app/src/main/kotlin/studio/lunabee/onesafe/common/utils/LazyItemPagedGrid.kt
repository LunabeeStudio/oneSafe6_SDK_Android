package studio.lunabee.onesafe.common.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.lunabee.lbcore.helper.rememberShowDelayedLoading
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.extensions.isLoading
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.commonui.extension.disableCanvas
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSShimmerSafeItem
import studio.lunabee.onesafe.ui.res.OSDimens

object LazyItemPagedGrid {

    @Composable
    fun <T : PlainItemData> rememberShowDelayedLoading(
        items: LazyPagingItems<T>,
    ): State<Boolean> {
        return rememberShowDelayedLoading(
            shouldShowLoading = items.itemCount == 0 && items.isLoading(),
            shouldHideLoading = !items.isLoading(),
            minLoadingShowDuration = AppConstants.Ui.DelayedLoading.MinDuration,
            delayBeforeShow = AppConstants.Ui.DelayedLoading.DelayBeforeShow,
        )
    }

    context(LazyGridScope)
    fun items(
        placeholders: Int,
        itemPagination: LazyPagingItems<PlainItemData>,
        onItemClick: (PlainItemData) -> Unit,
        itemsLayout: ItemsLayout,
        shouldDisabledItem: (PlainItemData) -> Boolean = { false },
    ) {
        if (placeholders > 0) {
            elementsPaginationPlaceholder(
                itemsCount = placeholders,
                inputLayout = itemsLayout,
            )
        } else {
            elementsPagination(
                itemsPagination = itemPagination,
                onItemClick = onItemClick,
                shouldDisabledItem = shouldDisabledItem,
                inputLayout = itemsLayout,
            )
        }
    }

    context(LazyGridScope)
    private fun elementsPaginationPlaceholder(
        itemsCount: Int? = null,
        inputLayout: ItemsLayout,
    ) {
        val count = minOf(itemsCount ?: AppConstants.Pagination.DefaultPageLoadingSize, AppConstants.Pagination.DefaultPageLoadingSize)
        items(
            count = count,
            key = { index -> inputLayout.name + index },
            contentType = {
                when (inputLayout) {
                    ItemsLayout.Grid -> ContentTypeDataDefaultItem
                    ItemsLayout.List -> ContentTypeDataRowItem
                }
            },
            span = {
                when (inputLayout) {
                    ItemsLayout.Grid -> GridItemSpan(1)
                    ItemsLayout.List -> GridItemSpan(maxLineSpan)
                }
            },
        ) { index ->
            when (inputLayout) {
                ItemsLayout.Grid -> OSShimmerSafeItem(
                    style = LocalItemStyle.current.standardStyle,
                    modifier = Modifier
                        .padding(OSDimens.SystemSpacing.Regular),
                )
                ItemsLayout.List -> PlainItemDataRow.Shimmer(
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.Regular),
                    position = OSLazyCardContent.Position.fromIndex(index, count - 1),
                )
            }
        }
    }

    context(LazyGridScope)
    private fun elementsPagination(
        itemsPagination: LazyPagingItems<PlainItemData>,
        onItemClick: (PlainItemData) -> Unit,
        shouldDisabledItem: (PlainItemData) -> Boolean,
        inputLayout: ItemsLayout,
    ) {
        items(
            count = itemsPagination.itemCount,
            key = { idx ->
                val plainItemData = itemsPagination.peek(idx)
                // Add PlainItemData type to the key to not animate items when switching between grid and list layouts
                plainItemData?.let { it::class }?.simpleName.orEmpty() + (plainItemData?.id ?: idx)
            },
            span = { idx ->
                when (itemsPagination.peek(idx)) {
                    null -> when (inputLayout) {
                        ItemsLayout.Grid -> GridItemSpan(1)
                        ItemsLayout.List -> GridItemSpan(maxLineSpan)
                    }
                    is PlainItemDataDefault -> GridItemSpan(1)
                    is PlainItemDataRow -> GridItemSpan(maxLineSpan)
                }
            },
            contentType = { idx ->
                when (itemsPagination.peek(idx)) {
                    null -> when (inputLayout) {
                        ItemsLayout.Grid -> ContentTypeDataDefaultItem
                        ItemsLayout.List -> ContentTypeDataRowItem
                    }
                    is PlainItemDataDefault -> ContentTypeDataDefaultItem
                    is PlainItemDataRow -> ContentTypeDataRowItem
                }
            },
        ) { index ->
            val item = itemsPagination[index]
            if (item == null) {
                val shimmerStyle = LocalItemStyle.current
                when (shimmerStyle.layout) {
                    ItemsLayout.Grid -> OSShimmerSafeItem(
                        style = shimmerStyle.standardStyle,
                        modifier = Modifier
                            .padding(OSDimens.SystemSpacing.Regular),
                    )
                    ItemsLayout.List -> PlainItemDataRow.Shimmer(
                        modifier = Modifier
                            .padding(horizontal = OSDimens.SystemSpacing.Regular),
                        position = OSLazyCardContent.Position.fromIndex(index, itemsPagination.itemCount - 1),
                    )
                }
            } else {
                val modifier = if (shouldDisabledItem(item)) {
                    Modifier
                        .disableCanvas()
                } else {
                    Modifier
                }.animateItem()
                when (item) {
                    is PlainItemDataDefault -> item.Composable(
                        modifier = modifier,
                        onItemClick = onItemClick,
                    )
                    is PlainItemDataRow -> item.Composable(
                        modifier = modifier
                            .padding(horizontal = OSDimens.SystemSpacing.Regular),
                        onItemClick = onItemClick,
                        position = OSLazyCardContent.Position.fromIndex(index, itemsPagination.itemCount - 1),
                    )
                }
            }
        }
    }

    private const val ContentTypeDataDefaultItem: String = "ContentTypeDataDefaultItem"
    private const val ContentTypeDataRowItem: String = "ContentTypeDataRowItem"
}
