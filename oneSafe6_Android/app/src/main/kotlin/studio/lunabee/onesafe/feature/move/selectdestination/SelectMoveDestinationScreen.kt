package studio.lunabee.onesafe.feature.move.selectdestination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.compose.accessibility.state.rememberAccessibilityState
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.common.extensions.isEmptyAfterLoading
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.feature.move.CorruptDestinationSnackBarState
import studio.lunabee.onesafe.feature.move.WarningMoveIntoSelfSnackBarState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

@Composable
fun SelectMoveDestinationRoute(
    itemToMoveId: UUID?,
    itemToMoveName: String,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    navigateToDestination: (UUID) -> Unit,
    lazyGridState: LazyGridState,
    nestedScrollConnection: NestedScrollConnection,
    onMove: () -> Unit,
    viewModel: SelectMoveDestinationViewModel = hiltViewModel(),
) {
    val items: LazyPagingItems<PlainItemData> = viewModel.items.collectAsLazyPagingItems()
    val currentDestination by viewModel.currentItem.collectAsStateWithLifecycle()
    val corruptDestinationSnackBarVisual = CorruptDestinationSnackBarState.snackbarVisuals
    val warningMoveIntoSelfSnackBarVisual = WarningMoveIntoSelfSnackBarState.snackbarVisuals

    SelectMoveDestinationScreen(
        onClickOnItem = {
            if (it.id == itemToMoveId) {
                showSnackBar(warningMoveIntoSelfSnackBarVisual)
            } else if (it.itemNameProvider is ErrorNameProvider) {
                showSnackBar(corruptDestinationSnackBarVisual)
            } else {
                navigateToDestination(it.id)
            }
        },
        items = items,
        currentDestination = currentDestination,
        itemToMoveId = itemToMoveId,
        lazyGridState = lazyGridState,
        nestedScrollConnection = nestedScrollConnection,
        placeholders = viewModel.initialItemsCount,
        itemName = itemToMoveName,
        onMove = onMove,
    )
}

@Composable
fun SelectMoveDestinationScreen(
    onClickOnItem: (PlainItemData) -> Unit,
    items: LazyPagingItems<PlainItemData>,
    currentDestination: MoveCurrentDestination?,
    itemToMoveId: UUID?,
    itemName: String,
    lazyGridState: LazyGridState,
    nestedScrollConnection: NestedScrollConnection,
    placeholders: Int,
    onMove: () -> Unit,
    itemStyleHolder: ItemStyleHolder = LocalItemStyle.current,
) {
    val showLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(items)
    val isEmptyAfterLoading = items.isEmptyAfterLoading()
    val accessibilityManager: AccessibilityState = rememberAccessibilityState()

    Box(
        modifier = Modifier.testTag(UiConstants.TestTag.Screen.SelectMoveDestinationScreen),
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.SelectMoveDestinationItemGrid)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            state = lazyGridState,
            contentPadding = PaddingValues(
                top = OSDimens.SystemSpacing.Small,
                bottom = OSDimens.SystemSpacing.Regular,
            ),
            columns = GridCells.Adaptive(minSize = itemStyleHolder.standardStyle.elementSize + OSDimens.SystemSpacing.Regular * 2),
            horizontalArrangement = Arrangement.Start,
        ) {
            if (currentDestination == null) {
                SelectMoveDestinationFactory.addHomeMessageCard(this, itemName)
                if (accessibilityManager.isAccessibilityEnabled) {
                    SelectMoveDestinationFactory.addHomeAccessibilityLabel(this, onMove)
                }
            } else {
                SelectMoveDestinationFactory.addItemDestinationTitle(this, currentDestination, onMove)
            }

            lazyVerticalOSRegularSpacer()

            if (isEmptyAfterLoading) {
                SelectMoveDestinationFactory.addNoItemCard(this)
            } else {
                LazyItemPagedGrid.items(
                    placeholders = if (showLoading) placeholders else 0,
                    itemPagination = items,
                    onItemClick = onClickOnItem,
                    itemsLayout = itemStyleHolder.layout,
                ) {
                    it.itemNameProvider is ErrorNameProvider || it.id == itemToMoveId
                }
            }
        }
    }
}
