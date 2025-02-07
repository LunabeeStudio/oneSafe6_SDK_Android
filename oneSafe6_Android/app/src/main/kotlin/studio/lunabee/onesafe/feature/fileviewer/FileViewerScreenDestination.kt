package studio.lunabee.onesafe.feature.fileviewer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object FileViewerScreenDestination : OSDestination {
    const val FieldId: String = "FieldId"

    override val route: String = "fileViewer/{$FieldId}"

    fun getRoute(
        fieldId: UUID,
    ): String = route.replace("{$FieldId}", fieldId.toString())
}

fun NavGraphBuilder.fileViewerScreen(
    navigateBack: () -> Unit,
) {
    composable(
        route = FileViewerScreenDestination.route,
        arguments = listOf(
            navArgument(FileViewerScreenDestination.FieldId) {
                type = NavType.StringType
            },
        ),
    ) {
        FileViewerRoute(
            navigateBack = navigateBack,
        )
    }
}
