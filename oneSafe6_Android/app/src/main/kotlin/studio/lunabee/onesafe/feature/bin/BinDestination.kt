package studio.lunabee.onesafe.feature.bin

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import java.util.UUID

object BinDestination : OSDestination {
    override val route: String = "bin"
}

context(ComposeItemActionNavScope)
fun NavGraphBuilder.binScreen(
    navigateBack: () -> Unit,
    navigateToItemDetails: (UUID) -> Unit,
    showSnackBar: (SnackbarVisuals) -> Unit,
) {
    composable(
        route = BinDestination.route,
    ) {
        BinRoute(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
            showSnackBar = showSnackBar,
        )
    }
}
