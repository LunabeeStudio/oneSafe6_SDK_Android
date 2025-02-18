package studio.lunabee.onesafe.feature.move.movehost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.collections.immutable.ImmutableList
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.extensions.topAppBarElevation
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.feature.move.MoveActionState
import studio.lunabee.onesafe.feature.move.MoveDestinationUiData
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.crashlytics.CrashlyticsCustomKeys
import studio.lunabee.onesafe.crashlytics.CrashlyticsHelper
import studio.lunabee.onesafe.crashlytics.CrashlyticsUnknown
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.navigation.extension.popUpToItemId
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID

@Composable
fun MoveHostRoute(
    navigateToHome: () -> Unit,
    navigateToItem: (UUID) -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
    navigateBack: () -> Unit,
    viewModel: MoveHostViewModel = hiltViewModel(),
) {
    val moveNavController: NavHostController = rememberNavController()
    val uiState: MoveHostUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val breadCrumbItems: ImmutableList<MoveDestinationUiData> by viewModel.moveDestinationItems.collectAsStateWithLifecycle()
    val dialogState: DialogState? by viewModel.moveDialogState.collectAsStateWithLifecycle()
    val moveState: MoveActionState by viewModel.moveActionState.collectAsStateWithLifecycle()

    val state by moveNavController.currentBackStackEntryAsState()
    state?.let {
        val route = runCatching { it.destination.route }.getOrNull()
        LaunchedEffect(route) {
            CrashlyticsHelper.setCustomKey(
                CrashlyticsCustomKeys.MoveNavScreen,
                route ?: CrashlyticsUnknown,
            )
        }
    }

    when (moveState) {
        is MoveActionState.NavigateToItem -> {
            val snackbarVisuals = (moveState as MoveActionState.NavigateToItem).snackbarState.snackbarVisuals
            LaunchedEffect(moveState) {
                val destinationId: UUID? = (moveState as MoveActionState.NavigateToItem).itemId
                if (destinationId != null) {
                    navigateToItem(destinationId)
                } else {
                    navigateToHome()
                }
                showSnackBar(snackbarVisuals)
                viewModel.consumeMoveActionState()
            }
        }

        is MoveActionState.Error -> {
            (moveState as MoveActionState.Error).throwable?.let {
                showSnackBar(
                    ErrorSnackbarState(
                        error = it,
                        onClick = {
                            viewModel.consumeMoveActionState()
                        },
                    ).snackbarVisuals,
                )
            }
            viewModel.consumeMoveActionState()
        }
    }

    LaunchedEffect(moveNavController) {
        moveNavController.addOnDestinationChangedListener { _, destination, argument ->
            viewModel.updateBreadcrumb(destination.route, argument)
        }
    }

    MoveHostScreen(
        showSnackBar = showSnackBar,
        itemId = uiState.itemToMoveId,
        itemName = uiState.itemToMoveName,
        breadCrumbItems = breadCrumbItems,
        navController = moveNavController,
        onMove = { viewModel.moveItem(destinationUUID = breadCrumbItems.last().id) },
        onClickOnDestination = { uuid ->
            if (breadCrumbItems.lastOrNull()?.id != uuid) {
                moveNavController.popUpToItemId(uuid)
            }
        },
        navigateBack = {
            if (moveNavController.previousBackStackEntry != null) {
                moveNavController.popBackStack()
            } else {
                navigateBack()
            }
        },
        moveButtonState = if (breadCrumbItems.lastOrNull()?.id != uiState.initialParentId) {
            OSActionState.Enabled
        } else {
            OSActionState.Disabled
        },
    )

    dialogState?.DefaultAlertDialog()
}

@Composable
fun MoveHostScreen(
    navigateBack: () -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
    navController: NavHostController,
    itemId: UUID?,
    itemName: String,
    breadCrumbItems: List<MoveDestinationUiData>,
    onClickOnDestination: (itemId: UUID?) -> Unit,
    onMove: () -> Unit,
    moveButtonState: OSActionState,
) {
    val lazyGridState: LazyGridState = rememberLazyGridState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyGridState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.MoveHostScreen,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.matchParentSize(),
        ) {
            ElevatedTopAppBar(
                title = LbcTextSpec.StringResource(OSString.move_selectDestination_title),
                options = listOf(
                    topAppBarOptionNavBack(navigateBack),
                ),
                elevation = lazyGridState.topAppBarElevation,
            )

            MoveNavGraph(
                showSnackBar = showSnackBar,
                moveNavController = navController,
                itemId = itemId,
                itemName = itemName,
                lazyGridState = lazyGridState,
                nestedScrollConnection = nestedScrollConnection,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onMove = onMove,
            )

            MoveBreadcrumb(
                items = breadCrumbItems,
                onClickOnDestination = onClickOnDestination,
                onClickOnCancel = navigateBack,
                onClickOnMove = onMove,
                moveButtonState = moveButtonState,
            )
        }
    }
}
