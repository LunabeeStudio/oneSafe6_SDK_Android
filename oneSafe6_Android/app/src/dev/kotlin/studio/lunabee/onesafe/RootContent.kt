package studio.lunabee.onesafe

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import studio.lunabee.onesafe.debug.RootDrawer
import studio.lunabee.onesafe.feature.main.MainRoute

@Composable
fun RootContent(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    var breadcrumbNavController: NavHostController? by remember {
        mutableStateOf(null)
    }

    RootDrawer(
        navController = navController,
        breadcrumbNavController = breadcrumbNavController,
    ) {
        MainRoute(
            mainNavController = navController,
            setBreadcrumbNavController = { breadcrumbNavController = it },
            snackbarHostState = snackbarHostState,
        )
    }
}
