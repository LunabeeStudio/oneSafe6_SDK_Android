package studio.lunabee.onesafe.feature.favorite

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.common.extensions.isInitializing
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.itemactions.ComposeItemAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID
import kotlin.random.Random

context(ComposeItemActionNavScope)
@Composable
fun FavoriteRoute(
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ComposeItemAction(getSafeItemActionDelegate = viewModel)
    FavoriteScreen(
        uiState = uiState,
        navigateBack = navigateBack,
        navigateToItemDetails = navigateToItemDetails,
    )
}

@Composable
private fun FavoriteScreen(
    uiState: FavoriteUiState,
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    itemStyleHolder: ItemStyleHolder = LocalItemStyle.current,
) {
    val items = uiState.items.collectAsLazyPagingItems()

    val lazyGridState: LazyGridState = rememberLazyGridState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyGridState)
    val showLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(items)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.Favorite,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        val waitingForData = !showLoading && items.isInitializing()
        when {
            waitingForData -> {
                /* no-op */
            }
            uiState.itemsCount == 0 -> {
                FavoriteEmptyCard(
                    modifier = Modifier
                        .padding(OSDimens.SystemSpacing.Regular)
                        .padding(top = OSDimens.ItemTopBar.Height),
                )
            }
            else -> {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
                    state = lazyGridState,
                    contentPadding = PaddingValues(top = OSDimens.ItemTopBar.Height),
                    columns = GridCells.Adaptive(
                        minSize = itemStyleHolder.standardStyle.elementSize + OSDimens.SystemSpacing.Regular * 2,
                    ),
                ) {
                    lazyVerticalOSRegularSpacer()

                    LazyItemPagedGrid.items(
                        placeholders = if (showLoading) uiState.itemsCount else 0,
                        itemPagination = items,
                        onItemClick = { navigateToItemDetails(it.id) },
                        itemsLayout = itemStyleHolder.layout,
                    )

                    lazyVerticalOSRegularSpacer()
                }
            }
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            FavoriteTopBar(
                onBackClick = navigateBack,
                modifier = Modifier
                    .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
@OsDefaultPreview
fun FavoriteScreenPreview() {
    OSTheme {
        FavoriteScreen(
            uiState = FavoriteUiState(
                flowOf(
                    PagingData.from(
                        (0..500).map {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites({})) },
                            )
                        },
                    ),
                ),
                1,
            ),
            navigateBack = { },
            navigateToItemDetails = {},
        )
    }
}
