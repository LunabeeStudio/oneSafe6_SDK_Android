package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.paging.compose.LazyPagingItems
import studio.lunabee.compose.accessibility.LbcAccessibilityUtils.cleanForAccessibility
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSLazyCard
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.common.extensions.getStringFromUnknownSafeItem
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsCorruptedCardData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsDeletedCardData
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsElementLayout
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSLargeItemTitle
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID
import kotlin.math.ceil

object ItemDetailsLazyColumnItems {
    fun itemLargeTitle(
        lazyListScope: LazyListScope,
        icon: OSItemIllustration,
        itemNameProvider: OSNameProvider,
    ) {
        lazyListScope.item(
            key = ContentTypeLargeItemTitle,
            contentType = ContentTypeLargeItemTitle,
        ) {
            OSLargeItemTitle(
                title = itemNameProvider.name,
                icon = icon,
                modifier = Modifier.padding(vertical = OSDimens.SystemSpacing.Regular),
            )
        }
    }

    /**
     * Draw the items by respecting both the provide [itemsLayout] coming from compose and the the type of paged [children] coming from
     * the viewmodel
     */
    context(LazyListScope)
    @Suppress("LongParameterList")
    fun itemElementsTab(
        placeholders: Int,
        children: LazyPagingItems<PlainItemData>,
        navigateToItemDetails: (UUID) -> Unit,
        elementLayout: ItemDetailsElementLayout,
        positionInList: OSLazyCardContent.Position,
        itemsLayout: ItemsLayout,
    ) {
        val showPlaceholderItems: Boolean = placeholders > 0
        val pageLoadingSize = when (itemsLayout) {
            ItemsLayout.Grid -> AppConstants.Pagination.DefaultPageLoadingSize
            ItemsLayout.List -> AppConstants.Pagination.RowPageLoadingSize
        }
        val placeHolderItems: List<PlainItemData?> = MutableList(
            minOf(placeholders, pageLoadingSize),
        ) { null }

        val count = when (itemsLayout) {
            ItemsLayout.Grid -> if (showPlaceholderItems) {
                ceil(placeHolderItems.size / elementLayout.childPerRow.toFloat()).toInt()
            } else {
                ceil(children.itemCount / elementLayout.childPerRow.toFloat()).toInt()
            }
            ItemsLayout.List -> if (showPlaceholderItems) {
                placeHolderItems.size
            } else {
                children.itemCount
            }
        }

        items(
            count = count,
            key = { idx ->
                if (showPlaceholderItems) {
                    idx
                } else {
                    when (itemsLayout) {
                        ItemsLayout.Grid -> children.peek(idx * elementLayout.childPerRow)?.id
                        ItemsLayout.List -> children.peek(idx)?.id
                    } ?: idx
                }
            },
            contentType = {
                when (itemsLayout) {
                    ItemsLayout.Grid -> ContentTypeDataItemDefault
                    ItemsLayout.List -> ContentTypeDataItemRow
                }
            },
        ) { idx ->
            val position = OSLazyCardContent.Position.fromIndexPaged(
                idx = idx,
                lastIndex = count - 1,
                positionInParent = positionInList,
            )
            val plainItemData = if (children.itemCount > 0) {
                children.peek(idx)
            } else {
                null
            }
            when {
                plainItemData is PlainItemDataDefault || (plainItemData == null && itemsLayout == ItemsLayout.Grid) -> {
                    val items = if (showPlaceholderItems) {
                        placeHolderItems.takeAt(idx * elementLayout.childPerRow, elementLayout.childPerRow)
                    } else {
                        children.takeAt(idx * elementLayout.childPerRow, elementLayout.childPerRow)
                    }

                    OSLazyCard(position) { paddingValues ->
                        ItemDetailsChildrenRow(
                            children = items,
                            onItemClick = navigateToItemDetails,
                            modifier = Modifier.animateItem()
                                .padding(paddingValues),
                            spacing = elementLayout.childSpacing,
                            elementStyle = elementLayout.childStyle,
                        )
                    }
                }
                plainItemData is PlainItemDataRow -> {
                    children[idx] // consume the index not consumed by previous peek call
                    plainItemData.Composable(
                        modifier = Modifier.animateItem(),
                        onItemClick = { navigateToItemDetails(it.id) },
                        position = position,
                    )
                }
                plainItemData == null && itemsLayout == ItemsLayout.List -> {
                    PlainItemDataRow.Shimmer(
                        modifier = Modifier.animateItem(),
                        position = position,
                    )
                }
            }
        }
    }

    fun itemActionsCard(
        lazyListScope: LazyListScope,
        actions: List<SafeItemAction>,
        key: Any,
    ) {
        lazyListScope.item(
            key = key,
            contentType = ContentTypeActionCard,
        ) {
            OSCard(
                modifier = Modifier
                    .testTag(UiConstants.TestTag.Item.ItemDetailsRegularActionCard)
                    .fillMaxWidth(),
            ) {
                actions.forEachIndexed { index, action ->
                    action.actionButton(
                        contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                            index = index,
                            elementsCount = actions.size,
                        ),
                    ).Composable()
                }
            }
        }
    }

    fun deletedCard(lazyListScope: LazyListScope, deletedCardData: ItemDetailsDeletedCardData) {
        lazyListScope.item(
            key = KeyDeletedCard,
            contentType = ContentTypeDeletedCard,
        ) {
            OSMessageCard(
                description = deletedCardData.message,
                modifier = Modifier.composed {
                    val clickLabel = deletedCardData.action.string
                    val text = deletedCardData.message.string.cleanForAccessibility()
                    clearAndSetSemantics {
                        this.text = AnnotatedString(text)
                        accessibilityClick(label = clickLabel, action = deletedCardData.onClick)
                    }
                },
            ) {
                OSTextButton(
                    text = deletedCardData.action,
                    onClick = deletedCardData.onClick,
                )
            }
        }
    }

    fun errorCard(lazyListScope: LazyListScope, errorCardData: ItemDetailsCorruptedCardData) {
        lazyListScope.item(
            key = KeyErrorCard,
            contentType = ContentTypeErrorCard,
        ) {
            OSMessageCard(
                description = errorCardData.message,
                modifier = Modifier
                    .composed {
                        val text = errorCardData.message.string.cleanForAccessibility()
                        clearAndSetSemantics {
                            this.text = AnnotatedString(text)
                        }
                    },
                title = errorCardData.title,
            )
        }
    }

    fun notFullySupportFieldsCard(lazyListScope: LazyListScope, notSupportedKindsList: List<SafeItemFieldKind.Unknown>) {
        lazyListScope.item(
            key = KeyNotSupportedFieldCard,
            contentType = ContentTypeNotSupportedCard,
        ) {
            val string = notSupportedKindsList.map { it.getStringFromUnknownSafeItem() }
                .toSet()
                .joinToString()
            OSMessageCard(
                title = LbcTextSpec.StringResource(OSString.safeItemDetail_notSupportedFields_title),
                description = LbcTextSpec.StringResource(OSString.safeItemDetail_notSupportedFields_message, string),
            )
        }
    }

    private const val ContentTypeLargeItemTitle: String = "ContentTypeLargeItemTitle"
    private const val ContentTypeDataItemDefault: String = "ContentTypeDataItemDefault"
    private const val ContentTypeDataItemRow: String = "ContentTypeDataItemRow"
    private const val ContentTypeActionCard: String = "ContentTypeActionCard"
    private const val ContentTypeDeletedCard: String = "ContentTypeDeletedCard"
    private const val ContentTypeErrorCard: String = "ContentTypeErrorCard"
    private const val ContentTypeNotSupportedCard: String = "ContentTypeNotSupportedCard"

    private const val KeyDeletedCard: String = "KeyDeletedCard"
    private const val KeyErrorCard: String = "KeyErrorCard"
    private const val KeyNotSupportedFieldCard: String = "KeyNotSupportedFieldCard"
}

private fun <T : Any> LazyPagingItems<T>.takeAt(index: Int, n: Int): List<T?> {
    val subList = mutableListOf<T?>()
    var idx = index

    while (itemCount > idx && idx < index + n) {
        subList += get(idx)
        idx++
    }

    return subList
}

private fun <T : Any?> List<T?>.takeAt(index: Int, n: Int): List<T?> {
    val subList = mutableListOf<T?>()
    var idx = index

    while (size > idx && idx < index + n) {
        subList += get(idx)
        idx++
    }

    return subList
}
