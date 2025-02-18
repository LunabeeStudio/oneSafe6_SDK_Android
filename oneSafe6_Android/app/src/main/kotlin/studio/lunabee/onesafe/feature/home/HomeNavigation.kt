package studio.lunabee.onesafe.feature.home

import androidx.navigation.NavController
import studio.lunabee.onesafe.feature.bin.BinDestination
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavigation
import studio.lunabee.onesafe.feature.favorite.FavoriteDestination
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import java.util.UUID

class HomeNavigation(
    navController: NavController,
    showBreadcrumb: (Boolean) -> Unit,
    breadcrumbNavigation: BreadcrumbNavigation,
) {
    val navigateToItemDetails: (UUID) -> Unit = { navController.safeNavigate(ItemDetailsDestination.getRoute(it)) }

    val navigateToBin: () -> Unit = { navController.safeNavigate(BinDestination.route) }

    val navigateToFavorite: () -> Unit = {
        navController.safeNavigate(FavoriteDestination.route)
        showBreadcrumb(false)
    }

    val navigateToSettings: () -> Unit = breadcrumbNavigation.navigateToSettings
    val navigateToVerifyPassword: () -> Unit = breadcrumbNavigation.navigateToVerifyPassword
    val navigateToBubblesContacts: () -> Unit = breadcrumbNavigation.navigateToBubblesHome
    val navigateToBubblesConversation: (contactId: UUID) -> Unit = breadcrumbNavigation.navigateToConversation
    val navigateToBubblesOnBoarding: () -> Unit = breadcrumbNavigation.navigateToBubblesOnBoarding
    val navigateToImportItems: () -> Unit = breadcrumbNavigation.navigateToImportItems
    val navigateToBackupSettings: () -> Unit = breadcrumbNavigation.navigateToBackupSettings
}
