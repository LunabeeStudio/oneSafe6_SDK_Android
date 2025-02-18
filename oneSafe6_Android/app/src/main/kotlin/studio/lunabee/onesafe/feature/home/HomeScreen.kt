package studio.lunabee.onesafe.feature.home

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.OSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.common.extensions.isEmptyAfterLoading
import studio.lunabee.onesafe.common.extensions.isInitializing
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.common.utils.LocaleCompat
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.home.HomeInfoDataNavScope
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.extension.randomColor
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavigation
import studio.lunabee.onesafe.feature.home.model.HomeConversationSectionData
import studio.lunabee.onesafe.feature.home.model.HomeInfoSectionData
import studio.lunabee.onesafe.feature.home.model.ItemRowData
import studio.lunabee.onesafe.feature.itemactions.ComposeItemAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionDelegate
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionsBottomSheet
import studio.lunabee.onesafe.feature.supportus.HelpUsTranslateBottomSheet
import studio.lunabee.onesafe.feature.supportus.SupportUsHomeInfoData
import studio.lunabee.onesafe.feature.verifypassword.VerifyPasswordBottomSheet
import studio.lunabee.onesafe.importexport.ui.AutoBackupErrorHomeInfoData
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

context(ComposeItemActionNavScope)
@Composable
fun HomeRoute(
    homeNavigation: HomeNavigation,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val homeInfoSectionData by viewModel.homeInfoSectionData.collectAsStateWithLifecycle()
    val conversationsSectionData by viewModel.homeConversationSection.collectAsStateWithLifecycle()
    val areItemsBeingGenerated by viewModel.areItemsBeingGenerated.collectAsStateWithLifecycle()
    val snackbarState by viewModel.snackbarState.collectAsStateWithLifecycle()
    val shouldAskForTranslationSupport by viewModel.shouldAskForTranslationSupport.collectAsStateWithLifecycle()
    val hasPreventionWarnings: Boolean by viewModel.hasPreventionWarnings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isTouchExplorationEnabled = rememberOSAccessibilityState().isTouchExplorationEnabled

    ComposeItemAction(viewModel)
    HomeScreen(
        uiState = uiState,
        homeNavigation = homeNavigation,
        importDiscoverData = { prefill, tutorial ->
            if (isTouchExplorationEnabled) {
                Toast.makeText(
                    context,
                    context.getString(OSString.home_tutorialDialog_discover_accessibility_feedback_inProgress),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            viewModel.importDiscoveryData(
                importExampleItems = tutorial,
                importPrefillItem = prefill,
                locale = LocaleCompat.getMainLocale(context),
            )
        },
        areItemsBeingGenerated = areItemsBeingGenerated,
        isBubblesShown = viewModel.osFeatureFlags.bubbles(),
        shouldAskForTranslationSupport = shouldAskForTranslationSupport,
        onTranslationBottomSheetDismissed = viewModel::blockTranslationCount,
        homeInfoSectionData = homeInfoSectionData,
        conversationsSectionData = conversationsSectionData,
        itemDisplayOptionDelegate = viewModel as ItemDisplayOptionDelegate,
        onClickLockSafe = viewModel::lockSafe,
        hasPreventionWarnings = hasPreventionWarnings,
    )

    LaunchedEffect(key1 = LocaleCompat.getMainLocale(context)) {
        viewModel.shouldAskForTranslationSupport(LocaleCompat.getMainLocale(context))
    }

    val snackbarVisual = snackbarState?.snackbarVisuals
    LaunchedEffect(snackbarState) {
        snackbarVisual?.let(showSnackBar)
    }
}

@Composable
fun HomeScreen(
    uiState: HomeScreenUiState,
    homeNavigation: HomeNavigation,
    importDiscoverData: (prefill: Boolean, tutorial: Boolean) -> Unit,
    areItemsBeingGenerated: Boolean,
    isBubblesShown: Boolean,
    shouldAskForTranslationSupport: Boolean,
    onTranslationBottomSheetDismissed: () -> Unit,
    onClickLockSafe: () -> Unit,
    homeInfoSectionData: HomeInfoSectionData,
    itemDisplayOptionDelegate: ItemDisplayOptionDelegate,
    conversationsSectionData: HomeConversationSectionData,
    hasPreventionWarnings: Boolean,
) {
    val items: LazyPagingItems<PlainItemData> = uiState.items.collectAsLazyPagingItems()
    val favoriteItems: ImmutableList<ItemRowData> by uiState.favoriteItems.collectAsStateWithLifecycle(
        HomeScreenUiState.InitialLoadingFavoritesList,
    )
    val showLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(items)
    var isVerifyPasswordBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isDiscoverBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isHelpTranslateBottomSheetVisible by rememberSaveable(shouldAskForTranslationSupport) {
        mutableStateOf(value = shouldAskForTranslationSupport)
    }
    var isFirstComposition by rememberSaveable { mutableStateOf(value = true) }
    val uriHandler = LocalUriHandler.current
    OSScreen(UiConstants.TestTag.Screen.Home) {
        val waitingForData = !uiState.isSafeReady || (!showLoading && items.isInitializing() && !areItemsBeingGenerated)
        val showEmptyScreen = uiState.isSafeReady
            && (
                areItemsBeingGenerated
                    || (!showLoading && items.isEmptyAfterLoading() && uiState.deletedItemCount == 0 && conversationsSectionData.isEmpty())
                )
        val lazyGridState = key(waitingForData, showEmptyScreen) { rememberLazyGridState() }
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyGridState)
        when {
            waitingForData -> {
                /* no-op */
            }
            showEmptyScreen -> HomeEmptyLayout(
                importDiscoverData = { isDiscoverBottomSheetVisible = true },
                areItemsBeingGenerated = areItemsBeingGenerated,
                navigateToImportSafe = homeNavigation.navigateToImportItems,
            )
            else -> {
                ItemGrid(
                    itemPagination = items,
                    favoriteRowDataList = favoriteItems,
                    conversationsSectionData = conversationsSectionData,
                    deletedItemCount = uiState.deletedItemCount,
                    showFavoritesSeeAll = uiState.showFavoritesSeeAll,
                    navigateToItemDetails = homeNavigation.navigateToItemDetails,
                    navigateToFavorite = homeNavigation.navigateToFavorite,
                    navigateToBin = homeNavigation.navigateToBin,
                    navigateToSettings = homeNavigation.navigateToSettings,
                    onClickOnVerifyPassword = if (uiState.isBiometricEnabled) fun() { isVerifyPasswordBottomSheetVisible = true } else null,
                    onClickOnCommunity = { uriHandler.openUri(CommonUiConstants.ExternalLink.Discord) },
                    onClickHelpTranslate = if (uiState.isLanguageGenerated) fun() { isHelpTranslateBottomSheetVisible = true } else null,
                    placeholders = if (showLoading || !uiState.isSafeReady) uiState.initialItemCount else 0,
                    homeInfoSectionData = homeInfoSectionData,
                    navigateToBackupSettings = homeNavigation.navigateToBackupSettings,
                    navigateToBubblesOnBoarding = homeNavigation.navigateToBubblesOnBoarding,
                    navigateToBubblesContacts = homeNavigation.navigateToBubblesContacts,
                    navigateToBubblesConversation = homeNavigation.navigateToBubblesConversation,
                    onClickLockSafe = onClickLockSafe,
                    lazyGridState = lazyGridState,
                    nestedScrollConnection = nestedScrollConnection,
                )
            }
        }
        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(all = OSDimens.SystemSpacing.Regular),
            ) {
                HomeTopActions(
                    navigateToSettings = homeNavigation.navigateToSettings,
                    navigateToBubbles = if (uiState.hasDoneOnBoardingBubbles) {
                        homeNavigation.navigateToBubblesContacts
                    } else {
                        homeNavigation.navigateToBubblesOnBoarding
                    },
                    isBubblesShown = isBubblesShown,
                    itemDisplayOptionDelegate = itemDisplayOptionDelegate,
                    showBeta = uiState.isAppBetaVersion,
                    hasPreventionWarning = hasPreventionWarnings,
                )
            }
        }
        VerifyPasswordBottomSheet(
            isVisible = isVerifyPasswordBottomSheetVisible,
            onBottomSheetClosed = { isVerifyPasswordBottomSheetVisible = false },
            onClickOnVerify = {
                isVerifyPasswordBottomSheetVisible = false
                homeNavigation.navigateToVerifyPassword()
            },
        )

        DiscoverSettingsBottomSheet(
            isVisible = isDiscoverBottomSheetVisible,
            onBottomSheetClosed = { isDiscoverBottomSheetVisible = false },
            onCreate = importDiscoverData,
        )

        HelpUsTranslateBottomSheet(
            isVisible = isHelpTranslateBottomSheetVisible,
            onBottomSheetClosed = {
                isHelpTranslateBottomSheetVisible = false
                onTranslationBottomSheetDismissed()
            },
            onClickHelpUs = {
                onTranslationBottomSheetDismissed()
                uriHandler.openUri(CommonUiConstants.ExternalLink.Discord)
            },
        )
    }

    LaunchedEffect(isFirstComposition) {
        if (uiState.shouldVerifyPassword && isFirstComposition) {
            delay(500) // Add delay to have a smoother apparition
            isVerifyPasswordBottomSheetVisible = true
        }
        isFirstComposition = false
    }
}

@Composable
private fun ItemGrid(
    itemPagination: LazyPagingItems<PlainItemData>,
    favoriteRowDataList: ImmutableList<ItemRowData>,
    conversationsSectionData: HomeConversationSectionData,
    deletedItemCount: Int?,
    showFavoritesSeeAll: Boolean,
    navigateToItemDetails: (UUID) -> Unit,
    navigateToFavorite: () -> Unit,
    navigateToBin: () -> Unit,
    navigateToSettings: () -> Unit,
    onClickOnVerifyPassword: (() -> Unit)?,
    onClickOnCommunity: () -> Unit,
    onClickHelpTranslate: (() -> Unit)?,
    placeholders: Int,
    homeInfoSectionData: HomeInfoSectionData,
    styleHolder: ItemStyleHolder = LocalItemStyle.current,
    navigateToBackupSettings: () -> Unit,
    navigateToBubblesOnBoarding: () -> Unit,
    navigateToBubblesContacts: () -> Unit,
    navigateToBubblesConversation: (UUID) -> Unit,
    onClickLockSafe: () -> Unit,
    lazyGridState: LazyGridState,
    nestedScrollConnection: OSTopBarVisibilityNestedScrollConnection,
) {
    val isTouchExplorationEnabled = rememberOSAccessibilityState().isTouchExplorationEnabled
    val homeInfoNavScope = remember {
        object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = navigateToBackupSettings
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = navigateToBubblesOnBoarding
        }
    }
    LazyVerticalGrid(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .testTag(UiConstants.TestTag.Item.HomeItemGrid)
            .fillMaxSize(),
        state = lazyGridState,
        contentPadding = PaddingValues(vertical = OSDimens.SystemSpacing.Regular),
        columns = GridCells.Adaptive(minSize = styleHolder.standardStyle.elementSize + OSDimens.SystemSpacing.Regular * 2),
    ) {
        HomeLazyGridItems.settingSpacer(
            lazyGridScope = this,
        )
        with(homeInfoNavScope) {
            homeInfoSectionData.item(isTouchExplorationEnabled)
        }
        conversationsSectionData.section(
            navigateToBubblesContacts = navigateToBubblesContacts,
            navigateToBubblesConversation = navigateToBubblesConversation,
        )
        addFavoriteSection(
            favoriteRowDataList,
            showFavoritesSeeAll,
            navigateToItemDetails,
            navigateToFavorite,
            styleHolder.homeFavoriteStyle,
        )
        HomeLazyGridItems.sectionSpacer()
        addElementsSection(itemPagination, navigateToItemDetails, placeholders, styleHolder.layout)

        HomeLazyGridItems.sectionSpacer()
        addOtherSection(
            deletedItemCount = deletedItemCount,
            navigateToParameter = navigateToSettings,
            onClickOnVerifyPassword = onClickOnVerifyPassword,
            navigateToBin = navigateToBin,
            onClickOnCommunity = onClickOnCommunity,
            onClickHelpTranslate = onClickHelpTranslate,
            onClickLockSafe = onClickLockSafe,
        )

        HomeLazyGridItems.bottomSpacer(this)
    }
}

private fun LazyGridScope.addFavoriteSection(
    favoriteRowDataList: ImmutableList<ItemRowData>,
    showFavoritesSeeAll: Boolean,
    navigateToItemDetails: (UUID) -> Unit,
    navigateToFavorite: () -> Unit,
    itemStyle: OSSafeItemStyle,
) {
    val action: @Composable (RowScope.() -> Unit)? = if (showFavoritesSeeAll) {
        {
            OSTextButton(
                text = LbcTextSpec.StringResource(id = OSString.common_seeAll),
                onClick = navigateToFavorite,
                modifier = Modifier
                    .accessibilityInvisibleToUser(),
            )
        }
    } else {
        null
    }

    HomeLazyGridItems.sectionHeader(
        text = LbcTextSpec.StringResource(OSString.common_favorites),
        key = OSString.common_favorites,
        action = action,
        accessibilityModifier = Modifier
            .composed {
                val clickLabel = stringResource(id = OSString.home_section_favorites_seeAll_accessibility)
                semantics(mergeDescendants = true) {
                    accessibilityClick(label = clickLabel, action = navigateToFavorite)
                    heading()
                }
            },
    )

    HomeLazyGridItems.elementsFavorite(
        lazyGridScope = this,
        favoriteRowDataList = favoriteRowDataList,
        onItemClick = navigateToItemDetails,
        onMoreClick = navigateToFavorite,
        itemStyle = itemStyle,
    )
}

private fun LazyGridScope.addElementsSection(
    itemPagination: LazyPagingItems<PlainItemData>,
    navigateToItemDetails: (UUID) -> Unit,
    placeholders: Int,
    itemsLayout: ItemsLayout,
) {
    HomeLazyGridItems.sectionHeader(
        text = LbcTextSpec.StringResource(OSString.home_section_myElements_title),
        key = OSString.home_section_myElements_title,
        accessibilityModifier = Modifier
            .semantics { heading() },
    )

    if (placeholders == 0 && itemPagination.itemCount == 0) {
        HomeLazyGridItems.emptyElementCard(this)
    } else {
        LazyItemPagedGrid.items(
            placeholders = placeholders,
            itemPagination = itemPagination,
            onItemClick = { navigateToItemDetails(it.id) },
            itemsLayout = itemsLayout,
        )
    }
}

@Suppress("LongParameterList")
private fun LazyGridScope.addOtherSection(
    deletedItemCount: Int?,
    navigateToParameter: () -> Unit,
    onClickOnVerifyPassword: (() -> Unit)?,
    navigateToBin: () -> Unit,
    onClickOnCommunity: () -> Unit,
    onClickHelpTranslate: (() -> Unit)?,
    onClickLockSafe: () -> Unit,
) {
    HomeLazyGridItems.sectionHeader(
        text = LbcTextSpec.StringResource(OSString.common_other),
        key = OSString.common_other,
        accessibilityModifier = Modifier
            .semantics { heading() },
    )

    val actions = mutableListOf(
        OthersAction.Bin(deletedItemCount = deletedItemCount, onClick = navigateToBin),
        OthersAction.Community(onClick = onClickOnCommunity),
        OthersAction.Settings(navigateToParameter),
        OthersAction.Lock(onClickLockSafe),
    )

    onClickHelpTranslate?.let { actions.add(1, OthersAction.HelpTranslate(onClickHelpTranslate)) }
    onClickOnVerifyPassword?.let { actions.add(1, OthersAction.VerifyPassword(onClickOnVerifyPassword)) }

    HomeLazyGridItems.othersCardItem(
        lazyGridScope = this,
        actions = actions,
        key = OSString.common_other + "OtherCard".hashCode(),
        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
    )
}

@OsDefaultPreview
@Composable
private fun HomeScreenFilledPreview() {
    OSTheme {
        HomeScreen(
            uiState = HomeScreenUiState(
                items = flowOf(
                    PagingData.from(
                        List(500) {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites {}) },
                            )
                        },
                    ),
                ),
                initialItemCount = 10,
                favoriteItems = flowOf(
                    List(3) {
                        ItemRowData.Item(
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider(loremIpsum(1)),
                                icon = null,
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites {}) },
                            ),
                        )
                    }.toImmutableList(),
                ),
                deletedItemCount = 3,
                isBiometricEnabled = true,
                isLanguageGenerated = false,
                showFavoritesSeeAll = true,
                isAppBetaVersion = true,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
            homeNavigation = homeNavigationPreview(),
            importDiscoverData = { _, _ -> },
            areItemsBeingGenerated = false,
            isBubblesShown = true,
            shouldAskForTranslationSupport = false,
            onTranslationBottomSheetDismissed = {},
            homeInfoSectionData = HomeInfoSectionData(
                listOf(
                    SupportUsHomeInfoData(Instant.EPOCH, onDismiss = {}) {},
                    AutoBackupErrorHomeInfoData(
                        errorLabel = loremIpsumSpec(1),
                        errorFull = loremIpsumSpec(10),
                        visibleSince = Instant.EPOCH,
                    ) {},
                ),
            ),
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            conversationsSectionData = HomeConversationSectionData(
                listOf(
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = true,
                    ),
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = false,
                    ),
                ),
            ),
            onClickLockSafe = {},
            hasPreventionWarnings = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeScreenFilledFullFavoriteErrorPreview() {
    OSTheme {
        HomeScreen(
            uiState = HomeScreenUiState(
                items = flowOf(
                    PagingData.from(
                        List(500) {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites {}) },
                            )
                        },
                    ),
                ),
                initialItemCount = 10,
                favoriteItems = flowOf(
                    List(8) {
                        if (it == 7) {
                            ItemRowData.More(8)
                        } else {
                            ItemRowData.Item(
                                PlainItemDataDefault(
                                    id = UUID.randomUUID(),
                                    itemNameProvider = DefaultNameProvider(loremIpsum(1)),
                                    icon = null,
                                    color = randomColor,
                                    actions = { listOf(SafeItemAction.AddToFavorites {}) },
                                ),
                            )
                        }
                    }.toImmutableList(),
                ),
                deletedItemCount = 8,
                isBiometricEnabled = true,
                isLanguageGenerated = false,
                showFavoritesSeeAll = true,
                isAppBetaVersion = true,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
            homeNavigation = homeNavigationPreview(),
            importDiscoverData = { _, _ -> },
            areItemsBeingGenerated = false,
            isBubblesShown = true,
            shouldAskForTranslationSupport = false,
            onTranslationBottomSheetDismissed = {},
            homeInfoSectionData = HomeInfoSectionData(emptyList()),
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            conversationsSectionData = HomeConversationSectionData(
                listOf(
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = true,
                    ),
                    BubblesConversationInfo(
                        id = createRandomUUID(),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = false,
                    ),
                ),
            ),
            onClickLockSafe = {},
            hasPreventionWarnings = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeScreenFullLoadingPreview() {
    OSTheme {
        HomeScreen(
            uiState = HomeScreenUiState(
                items = flowOf(
                    PagingData.from(
                        List(500) {
                            PlainItemDataDefault(
                                id = UUID.randomUUID(),
                                itemNameProvider = DefaultNameProvider("${loremIpsum(1)}_$it"),
                                icon = iconSample.takeIf { Random.nextBoolean() },
                                color = randomColor,
                                actions = { listOf(SafeItemAction.AddToFavorites {}) },
                            )
                        },
                        LoadStates(
                            refresh = LoadState.Loading,
                            prepend = LoadState.Loading,
                            append = LoadState.Loading,
                        ),
                    ),
                ),
                initialItemCount = 10,
                favoriteItems = flowOf(List(AppConstants.Ui.HomeFavorite.MaxShowAmount) { ItemRowData.Loading }.toImmutableList()),
                deletedItemCount = 0,
                isBiometricEnabled = true,
                isLanguageGenerated = true,
                showFavoritesSeeAll = true,
                isAppBetaVersion = true,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
            homeNavigation = homeNavigationPreview(),
            importDiscoverData = { _, _ -> },
            areItemsBeingGenerated = false,
            isBubblesShown = true,
            shouldAskForTranslationSupport = false,
            onTranslationBottomSheetDismissed = {},
            homeInfoSectionData = HomeInfoSectionData(emptyList()),
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            conversationsSectionData = HomeConversationSectionData(emptyList()),
            onClickLockSafe = {},
            hasPreventionWarnings = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeScreenEmptyPreview() {
    OSTheme {
        HomeScreen(
            uiState = HomeScreenUiState(
                items = flowOf(
                    PagingData.empty(
                        LoadStates(
                            refresh = LoadState.NotLoading(false),
                            prepend = LoadState.NotLoading(false),
                            append = LoadState.NotLoading(false),
                        ),
                    ),
                ),
                initialItemCount = 10,
                favoriteItems = flowOf(persistentListOf()),
                deletedItemCount = 0,
                isBiometricEnabled = false,
                isLanguageGenerated = false,
                showFavoritesSeeAll = false,
                isAppBetaVersion = false,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
            homeNavigation = homeNavigationPreview(),
            importDiscoverData = { _, _ -> },
            areItemsBeingGenerated = false,
            isBubblesShown = true,
            shouldAskForTranslationSupport = false,
            onTranslationBottomSheetDismissed = {},
            homeInfoSectionData = HomeInfoSectionData(emptyList()),
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            conversationsSectionData = HomeConversationSectionData(emptyList()),
            onClickLockSafe = {},
            hasPreventionWarnings = true,
        )
    }
}

@Composable
@Preview
private fun homeNavigationPreview(): HomeNavigation = HomeNavigation(
    rememberNavController(),
    {},
    BreadcrumbNavigation(
        mainNavController = rememberNavController(),
        navigateBack = {},
        onCompositionNav = { null },
    ),
)
