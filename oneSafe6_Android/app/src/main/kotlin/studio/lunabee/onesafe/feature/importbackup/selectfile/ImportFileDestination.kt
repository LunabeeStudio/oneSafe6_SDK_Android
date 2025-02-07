package studio.lunabee.onesafe.feature.importbackup.selectfile

import android.net.Uri
import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination

object ImportFileDestination : OSDestination {
    const val dataArg: String = "data"
    const val deleteFileArg: String = "deleteFile"
    private const val path: String = "importFile"

    override val route: String = "$path?$dataArg={$dataArg}&$deleteFileArg={$deleteFileArg}"

    val arguments: List<NamedNavArgument> = listOf(
        navArgument(dataArg) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(deleteFileArg) {
            type = NavType.BoolType
        },
    )

    fun getRoute(data: Uri?, deleteOnComplete: Boolean): String {
        val builder = Uri.Builder()
        builder.path(path)
        data?.let {
            builder.appendQueryParameter(dataArg, data.toString())
        }
        builder.appendQueryParameter(deleteFileArg, deleteOnComplete.toString())
        return builder.toString()
    }
}

fun NavGraphBuilder.importFileScreen(
    navigateBack: () -> Unit,
    navigateToImportAuthDestination: () -> Unit,
    navigateToWarningNotFullySupportedArchive: () -> Unit,
    showSnackBar: ((SnackbarVisuals) -> Unit)?,
) {
    composable(
        route = ImportFileDestination.route,
        arguments = ImportFileDestination.arguments,
    ) {
        ImportFileRoute(
            navigateBack = navigateBack,
            navigateToImportAuthDestination = navigateToImportAuthDestination,
            navigateToWarningNotFullySupportedArchive = navigateToWarningNotFullySupportedArchive,
            showSnackBar = showSnackBar,
        )
    }
}
