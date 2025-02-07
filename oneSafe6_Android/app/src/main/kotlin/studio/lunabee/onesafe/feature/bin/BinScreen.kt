package studio.lunabee.onesafe.feature.bin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.common.extensions.isEmptyAfterLoading
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.bin.model.BinGlobalAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID
import kotlin.random.Random

context(ComposeItemActionNavScope)
@Composable
fun BinRoute(
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
    viewModel: BinViewModel = hiltViewModel(),
) {
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    // TODO validate this approach for lifecycle vs paging @rlatapy-luna
    //  http://disq.us/p/2sfqnab
    val items: LazyPagingItems<PlainItemData> = remember(key1 = viewModel.items, key2 = lifecycleOwner) {
        viewModel.items.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }.collectAsLazyPagingItems()
    val isEmptyAfterLoading = items.isEmptyAfterLoading()

    ComposeItemAction(viewModel)

    val snackbarState by viewModel.snackbarState.collectAsStateWithLifecycle(null)
    snackbarState?.snackbarVisuals?.let { snackbarVisuals ->
        LaunchedEffect(snackbarVisuals) {
            showSnackBar(snackbarVisuals)
        }
    }

    OSScreen(UiConstants.TestTag.Screen.Bin) {
        if (isEmptyAfterLoading) {
            BinEmptyScreenContent(
                navigateBack = navigateBack,
            )
        } else {
            BinDataScreenContent(
                items = items,
                navigateBack = navigateBack,
                navigateToItemDetails = navigateToItemDetails,
                actions = viewModel.getActions(navigateBack),
                itemsCount = viewModel.initialDeletedItemCount,
            )
        }

        dialogState?.DefaultAlertDialog()
    }
}

@Composable
private fun BoxScope.BinDataScreenContent(
    items: LazyPagingItems<PlainItemData>,
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    actions: LinkedHashSet<BinGlobalAction>?,
    itemsCount: Int,
    itemStyle: ItemStyleHolder = LocalItemStyle.current,
) {
    val lazyGridState: LazyGridState = rememberLazyGridState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyGridState)
    val showLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(items)

    LazyVerticalGrid(
        modifier = Modifier
            .testTag(UiConstants.TestTag.Item.BinItemGrid)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        state = lazyGridState,
        contentPadding = PaddingValues(top = OSDimens.ItemTopBar.Height),
        columns = GridCells.Adaptive(minSize = itemStyle.standardStyle.elementSize + OSDimens.SystemSpacing.Regular * 2),
    ) {
        lazyVerticalOSRegularSpacer()

        item(
            key = KeyInfoCard,
            span = { GridItemSpan(currentLineSpan = maxLineSpan) },
        ) {
            OSMessageCard(
                description = LbcTextSpec.StringResource(id = OSString.bin_infoCard_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
        }

        lazyVerticalOSRegularSpacer()

        LazyItemPagedGrid.items(
            placeholders = if (showLoading) itemsCount else 0,
            itemPagination = items,
            onItemClick = { navigateToItemDetails(it.id) },
            itemsLayout = itemStyle.layout,
        )

        lazyVerticalOSRegularSpacer()
    }

    OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
        BinTopBar(
            onBackClick = navigateBack,
            actions = actions,
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                .statusBarsPadding()
                .align(Alignment.TopCenter),
        )
    }
}

private const val KeyInfoCard: String = "KeyInfoCard"

@Composable
private fun BoxScope.BinEmptyScreenContent(
    navigateBack: () -> Unit,
) {
    val localDensity = LocalDensity.current
    val isImageDisplayed = remember(localDensity) { mutableStateOf(true) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val scrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        OSMessageCard(
            description = LbcTextSpec.StringResource(OSString.bin_empty_card_description),
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(OSDimens.SystemSpacing.Regular),
            title = LbcTextSpec.StringResource(OSString.bin_empty_card_title),
        )
        if (isImageDisplayed.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .weight(1f)
                    .padding(top = OSDimens.SystemSpacing.Regular)
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            ) {
                Image(
                    painter = painterResource(id = OSDrawable.character_bin),
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(AppConstants.Ui.Bin.MaxWidthRatioImageEmptyScreen)
                        .onPlaced {
                            with(localDensity) {
                                isImageDisplayed.value =
                                    it.size.height.toDp() > screenHeight * AppConstants.Ui.Bin.MinHeightRatioImageEmptyScreen
                            }
                        },
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                    alignment = Alignment.BottomEnd,
                )
            }
        }
    }

    OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
        BinTopBar(
            onBackClick = navigateBack,
            actions = null,
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                .statusBarsPadding()
                .align(Alignment.TopCenter),
        )
    }
}

@Composable
@OsDefaultPreview
fun BinDataScreenPreview() {
    OSTheme {
        OSScreen("") {
            BinDataScreenContent(
                items = flowOf(
                    PagingData.from(
                        List<PlainItemData>(500) {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites({})) },
                            )
                        },
                    ),
                ).collectAsLazyPagingItems(),
                navigateBack = { },
                navigateToItemDetails = {},
                actions = linkedSetOf(BinGlobalAction.RestoreAll {}, BinGlobalAction.RemoveAll {}),
                itemsCount = 10,
            )
        }
    }
}

@Composable
@OsDefaultPreview
fun BinEmptyScreenPreview() {
    OSTheme {
        OSScreen("") {
            BinEmptyScreenContent(
                navigateBack = {},
            )
        }
    }
}
