package studio.lunabee.onesafe.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.OSLoadingView
import studio.lunabee.onesafe.commonui.snackbar.ActionSnackbarVisuals
import studio.lunabee.onesafe.commonui.snackbar.NavigationSnackbarVisuals
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavGraphDestination
import studio.lunabee.onesafe.feature.forceupgrade.ForceUpgradeDestination
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.navigation.graph.BreadcrumbOnCompositionNav
import studio.lunabee.onesafe.navigation.graph.MainNavGraph
import studio.lunabee.onesafe.navigation.graph.OnBoardingNavGraphDestination
import studio.lunabee.onesafe.ui.UiConstants

private val logger = LBLogger.get("MainRoute")

@Composable
fun MainRoute(
    mainNavController: NavHostController,
    setBreadcrumbNavController: ((NavHostController) -> Unit)? = null,
    viewModel: MainViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.CREATED, // Allow collection on background
    )

    when (val state = sessionState) {
        is SessionState.Broken -> LaunchedEffect(mainNavController) {
            mainNavController.navigate(LoginDestination.route) {
                popUpTo(BreadcrumbNavGraphDestination.route) {
                    inclusive = true
                    saveState = true
                }
            }
            // Do not reset the broken state before the resumed state in case of activity re-creation + autolock. In this case, the above
            // autolock navigation happens on the "old" nav controller because a new one will be instantiate due to full re-composition. So
            // we need to keep the broken state until the new nav controller also navigate to the login.
            // ⚠️ Entry flow does not emit on lifecycle state change, so use the lifecycle observer API
            val lifecycle = mainNavController.currentBackStackEntryFlow.firstOrNull()?.lifecycle
            lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    lifecycle.removeObserver(this)
                    state.reset()
                }
            })
        }
        SessionState.Idle -> {
            /* no-op */
        }
    }

    val startDestination = remember {
        when {
            viewModel.isForceUpgradeDisplayed -> ForceUpgradeDestination.route
            viewModel.isUserSignUp -> LoginDestination.route
            else -> OnBoardingNavGraphDestination.route
        }
    }

    MainScreen(
        mainNavController = mainNavController,
        startDestination = startDestination,
        isUserSignUp = viewModel.isUserSignUp,
        snackbarHostState = snackbarHostState,
        setBreadcrumbNavController = setBreadcrumbNavController,
    )
}

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    startDestination: String,
    isUserSignUp: Boolean,
    snackbarHostState: SnackbarHostState,
    setBreadcrumbNavController: ((NavHostController) -> Unit)?,
) {
    val coroutineScope = rememberCoroutineScope()
    val containerColor = MaterialTheme.colorScheme.background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor,
    ) {
        Box(Modifier.fillMaxSize()) {
            MainNavGraph(
                showSnackBarWithNav = { snackbarVisuals, breadcrumbNavigate ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackBar(snackbarVisuals, breadcrumbNavigate)
                    }
                },
                mainNavController = mainNavController,
                navGraphStartDestinationRoute = startDestination,
                isUserSignUp = isUserSignUp,
                setBreadcrumbNavController = setBreadcrumbNavController,
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .zIndex(UiConstants.SnackBar.ZIndex)
                    .navigationBarsPadding()
                    .imePadding()
                    .align(Alignment.BottomCenter),
            )

            OSLoadingView()
        }
    }
}

private suspend fun SnackbarHostState.showSnackBar(
    visuals: SnackbarVisuals,
    breadcrumbNavigate: (BreadcrumbOnCompositionNav.Navigate) -> Unit,
) {
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
                    breadcrumbNavigate(BreadcrumbOnCompositionNav.Navigate(visuals.route))
                    visuals.onDismiss()
                }
                else -> logger.e("Unexpected snackbar action performed $visuals")
            }
        }
    }
}
