package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.lunabee.lblogger.LBLogger
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.snackbar.ActionSnackbarVisuals
import studio.lunabee.onesafe.commonui.snackbar.NavigationSnackbarVisuals
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.home.HomeDestination
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemactions.ComposeItemAction
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.feature.itemform.bottomsheet.CreateNewItemBottomSheet
import studio.lunabee.onesafe.feature.search.holder.SearchUiState
import studio.lunabee.onesafe.feature.search.screen.SearchBottomSheet
import studio.lunabee.onesafe.navigation.extension.popUpToItemId
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

private val logger = LBLogger.get("BreadcrumbRoute")

@Composable
fun BreadcrumbRoute(
    breadcrumbNavigation: BreadcrumbNavigation,
    breadcrumbNavController: NavHostController,
    viewModel: BreadcrumbViewModel = hiltViewModel(),
) {
    val searchTextValue: String by viewModel.searchTextValue.collectAsStateWithLifecycle()

    val isSearchActive by remember { derivedStateOf { searchTextValue.isNotBlank() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var isCreateNewItemBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isSearchBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    with(
        object : ComposeItemActionNavScope {
            override val showSnackbar: (visuals: SnackbarVisuals) -> Unit = { snackbarVisuals ->
                coroutineScope.launch {
                    snackbarHostState.showSnackBar(snackbarVisuals, breadcrumbNavController)
                }
            }
            override val navigateToMove: (itemId: UUID) -> Unit = breadcrumbNavigation.navigateToMove
            override val navigateToShare: (itemId: UUID, includeChildren: Boolean) -> Unit = breadcrumbNavigation.navigateToShare
            override val navigateBack: () -> Unit = { breadcrumbNavController.popBackStack() }
            override val navigateToSendViaBubbles: (itemId: UUID, includeChildren: Boolean) -> Unit =
                breadcrumbNavigation.navigateToSendItemViaBubbles
        },
    ) {
        when (val state = uiState) {
            BreadcrumbUiState.Initializing -> {
                BreadcrumbScreen(
                    breadcrumbItems = persistentListOf(),
                    currentColor = null,
                    navController = breadcrumbNavController,
                    isInSearchMode = false,
                    isSearchActive = false,
                    onSearchClick = {},
                    showNewItemBottomSheet = {},
                    onBreadcrumbMainClick = {},
                    snackbarHostState = snackbarHostState,
                    breadcrumbNavigation = breadcrumbNavigation,
                )
            }
            is BreadcrumbUiState.Idle -> {
                val breadcrumbItems = state.breadcrumbItems
                val itemColor = state.userColor
                val searchUiState: SearchUiState by viewModel.searchState.collectAsStateWithLifecycle()

                state.dialogState?.DefaultAlertDialog()

                LaunchedEffect(breadcrumbNavController) {
                    viewModel.initSearch()

                    breadcrumbNavController.addOnDestinationChangedListener { _, destination, arguments ->
                        // Breadcrumb stuff
                        val breadcrumbDestination = destination.route?.let { BreadcrumbDestinationSpec.fromRoute(it, arguments) }
                        breadcrumbDestination?.let(viewModel::updateBreadcrumb)

                        // SnackBar stuff
                        val snackbarRoute = (snackbarHostState.currentSnackbarData?.visuals as? NavigationSnackbarVisuals)?.route
                        if (snackbarRoute != null && destination.route == ItemDetailsDestination.route) {
                            val itemRoute = arguments?.getString(ItemDetailsDestination.itemIdArgument)
                                ?.let(UUID::fromString)
                                ?.let { itemId ->
                                    ItemDetailsDestination.getRoute(itemId)
                                }
                            if (snackbarRoute == itemRoute) {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    }
                }
                ComposeItemAction(getSafeItemActionDelegate = viewModel)

                // TODO <m3_search> can we factorize itemId in lambdas
                // Bottom sheets
                CreateNewItemBottomSheet(
                    isVisible = isCreateNewItemBottomSheetVisible,
                    onBottomSheetClosed = { isCreateNewItemBottomSheetVisible = false },
                    onItemWithTemplateClicked = { template ->
                        isCreateNewItemBottomSheetVisible = false
                        val itemId = (breadcrumbItems.lastOrNull()?.destination as? ItemBreadcrumbDestination)?.itemId
                        breadcrumbNavigation.navigateToItemCreationFromTemplate(
                            template.template,
                            itemId,
                            itemColor,
                            (template as? ItemCreationEntryWithTemplate.WebsiteFromClipboard)?.clipboardContent,
                        )
                    },
                    onFileSelected = { uriList ->
                        isCreateNewItemBottomSheetVisible = false
                        val itemId = (breadcrumbItems.lastOrNull()?.destination as? ItemBreadcrumbDestination)?.itemId
                        breadcrumbNavigation.navigateToItemCreationFromFileUrl(
                            uriList,
                            itemId,
                            itemColor,
                        )
                    },
                    cameraData = state.cameraForField,
                    onImageCaptureFromCameraResult = { cameraData ->
                        isCreateNewItemBottomSheetVisible = false
                        val itemId = (breadcrumbItems.lastOrNull()?.destination as? ItemBreadcrumbDestination)?.itemId
                        breadcrumbNavigation.navigateToItemCreationFromCamera(
                            itemId,
                            itemColor,
                            cameraData,
                        )
                    },
                )

                SearchBottomSheet(
                    isVisible = isSearchBottomSheetVisible,
                    onBottomSheetClosed = { isSearchBottomSheetVisible = false },
                    searchState = searchUiState,
                    onRecentSearchClick = viewModel::clickOnRecentSearch,
                    onValueSearchChange = viewModel::search,
                    searchValue = searchTextValue,
                    onItemClick = {
                        viewModel.saveActualSearchRecentSearch()
                        isSearchBottomSheetVisible = false
                        breadcrumbNavController.safeNavigate(route = ItemDetailsDestination.getRoute(it)) {
                            popUpTo(HomeDestination.route)
                        }
                    },
                )

                BreadcrumbScreen(
                    breadcrumbItems = breadcrumbItems,
                    currentColor = itemColor,
                    navController = breadcrumbNavController,
                    isInSearchMode = isSearchBottomSheetVisible,
                    isSearchActive = isSearchActive,
                    onSearchClick = {
                        viewModel.initSearchIndex()
                        isSearchBottomSheetVisible = true
                    },
                    showNewItemBottomSheet = { isCreateNewItemBottomSheetVisible = true },
                    onBreadcrumbMainClick = viewModel::onBreadcrumbMainClick,
                    snackbarHostState = snackbarHostState,
                    breadcrumbNavigation = breadcrumbNavigation,
                )
            }
        }
    }
}

context (ComposeItemActionNavScope)
@Composable
fun BreadcrumbScreen(
    breadcrumbItems: ImmutableList<BreadcrumbUiDataSpec>,
    currentColor: Color?,
    navController: NavHostController,
    isInSearchMode: Boolean,
    isSearchActive: Boolean,
    onSearchClick: () -> Unit,
    showNewItemBottomSheet: () -> Unit,
    onBreadcrumbMainClick: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
    breadcrumbNavigation: BreadcrumbNavigation,
) {
    val coroutineScope = rememberCoroutineScope()
    var showBreadcrumb by remember { mutableStateOf(true) }
    Scaffold(
        modifier = Modifier.testTag(UiConstants.TestTag.Screen.BreadcrumbScreen),
        bottomBar = {
            AnimatedVisibility(
                visible = showBreadcrumb,
                enter = expandVertically(
                    tween(
                        AppConstants.Ui.Animation.Breadcrumb.ExpandDurationMs,
                        AppConstants.Ui.Animation.Breadcrumb.ExpandDelayMs,
                    ),
                    Alignment.Top,
                ),
                exit = shrinkVertically(
                    tween(
                        AppConstants.Ui.Animation.Breadcrumb.ShrinkDurationMs,
                        AppConstants.Ui.Animation.Breadcrumb.ShrinkDelayMs,
                    ),
                ),
            ) {
                val onAddClick = when (breadcrumbItems.lastOrNull()?.mainAction) {
                    BreadcrumbMainAction.AddItem -> showNewItemBottomSheet
                    BreadcrumbMainAction.None -> null
                    else -> onBreadcrumbMainClick
                }

                val isBreadCrumbFullyVisible = transition.targetState == EnterExitState.Visible
                    && transition.currentState == EnterExitState.Visible

                Breadcrumb(
                    isFullyVisible = isBreadCrumbFullyVisible,
                    navigate = { destination ->
                        when (destination) {
                            is ItemBreadcrumbDestination -> navController.popUpToItemId(destination.itemId)
                            is HardBreadcrumbDestination -> {
                                if (navController.currentDestination?.route != destination.route) {
                                    val hasPop = navController.popBackStack(destination.route, false)
                                    if (!hasPop) { // If destination was not in the backstack, pop to the home before navigate
                                        navController.safeNavigate(destination.route) {
                                            launchSingleTop = true
                                            popUpTo(HomeDestination.route)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    isSearchActive = isSearchActive,
                    onSearchClick = onSearchClick,
                    onAddClick = onAddClick,
                    items = breadcrumbItems,
                    isSearchVisible = isInSearchMode,
                    color = currentColor,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .zIndex(UiConstants.SnackBar.ZIndex),
            )
        },
        contentWindowInsets = WindowInsets(bottom = OSDimens.SystemSpacing.None),
    ) { scaffoldPadding ->
        val padding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding())

        Box(
            Modifier.padding(padding),
        ) {
            BreadcrumbNavGraph(
                showSnackBar = { snackbarVisuals ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackBar(snackbarVisuals, navController)
                    }
                },
                outerNavigation = breadcrumbNavigation,
                showBreadcrumb = { show -> showBreadcrumb = show },
                navController = navController,
                isInSearchMode = isInSearchMode,
            )
        }
    }
}

private suspend fun SnackbarHostState.showSnackBar(visuals: SnackbarVisuals, navController: NavHostController) {
    val result = this.showSnackbar(visuals)
    when (result) {
        SnackbarResult.Dismissed -> when (visuals) {
            is ActionSnackbarVisuals -> visuals.onDismiss()
            is NavigationSnackbarVisuals -> visuals.onDismiss()
            else -> {
                /* no-op  : No need to call the onDismiss method*/
            }
        }
        SnackbarResult.ActionPerformed -> {
            when (visuals) {
                is ActionSnackbarVisuals -> {
                    visuals.action()
                    visuals.onDismiss()
                }
                is NavigationSnackbarVisuals -> {
                    navController.safeNavigate(visuals.route)
                    visuals.onDismiss()
                }
                else -> logger.e("Unexpected snackbar action performed $visuals")
            }
        }
    }
}
