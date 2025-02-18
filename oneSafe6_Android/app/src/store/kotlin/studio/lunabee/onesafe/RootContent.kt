package studio.lunabee.onesafe

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import studio.lunabee.onesafe.feature.main.MainRoute
import androidx.compose.material3.SnackbarHostState

@Composable
fun RootContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    MainRoute(
        mainNavController = navController,
        snackbarHostState = snackbarHostState,
    )
}
