package studio.lunabee.onesafe.feature.migration.savedata

import android.net.Uri
import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import studio.lunabee.onesafe.commonui.OSDestination

object MigrationSaveDataDestination : OSDestination {
    const val uriArgument: String = "uri"

    override val route: String = "migrationSaveData/{$uriArgument}"

    fun getRoute(
        uri: Uri,
    ): String = route.replace("{$uriArgument}", Uri.encode(uri.toString()))
}

fun NavGraphBuilder.migrationSaveDataGraph(
    navigateBack: () -> Unit,
    navigateToHomeScreen: () -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
) {
    composable(
        route = MigrationSaveDataDestination.route,
        arguments = listOf(
            navArgument(MigrationSaveDataDestination.uriArgument) {
                type = NavType.StringType
            },
        ),
        deepLinks = listOf(navDeepLink { uriPattern = MigrationSaveDataDestination.route }),
    ) {
        MigrationSaveDataRoute(
            navigateBack = navigateBack,
            navigateToHome = navigateToHomeScreen,
            showSnackBar = showSnackBar,
        )
    }
}
