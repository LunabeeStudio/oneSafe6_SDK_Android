package studio.lunabee.onesafe.feature.settings.navigation

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthDestination
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthRoute
import studio.lunabee.onesafe.feature.exportbackup.exportdata.ExportDataDestination
import studio.lunabee.onesafe.feature.exportbackup.exportdata.ExportDataRoute
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveDestination
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveRoute

@Suppress("LongParameterList")
fun NavGraphBuilder.exportBackupGraph(
    navigateBack: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
    navigateBackToSettingsDestination: () -> Unit,
    navigateToExportAuthDestination: () -> Unit,
    navigateToExportDataDestination: (itemCount: Int, contactCount: Int, safeNav: Boolean) -> Unit,
    navigateToExportGetArchiveDestination: (filePath: String) -> Unit,
) {
    composable(
        route = ExportAuthDestination.route,
    ) {
        ExportAuthRoute(
            navigateBack = navigateBack,
            navigateToExportDataDestination = navigateToExportDataDestination,
        )
    }

    composable(
        route = ExportDataDestination.route,
        arguments = listOf(
            navArgument(ExportDataDestination.ArgItemCount) {
                type = NavType.IntType
            },
            navArgument(ExportDataDestination.ArgContactCount) {
                type = NavType.IntType
            },
        ),
    ) {
        val itemCount = it.arguments?.getInt(ExportDataDestination.ArgItemCount)
        val contactCount = it.arguments?.getInt(ExportDataDestination.ArgContactCount)

        ExportDataRoute(
            navigateBack = navigateBack,
            navigateToExportGetArchiveDestination = navigateToExportGetArchiveDestination,
            itemCount = itemCount ?: 0,
            contactCount = contactCount ?: 0,
        )
    }

    composable(
        route = ExportGetArchiveDestination.route,
    ) {
        ExportGetArchiveRoute(
            showSnackBar = showSnackBar,
            navigateBackToSettingsDestination = navigateBackToSettingsDestination,
            navigateToExportAuthDestination = navigateToExportAuthDestination,
        )
    }
}
