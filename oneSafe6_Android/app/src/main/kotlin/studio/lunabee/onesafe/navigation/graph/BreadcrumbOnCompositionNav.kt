package studio.lunabee.onesafe.navigation.graph

import java.util.UUID

sealed interface BreadcrumbOnCompositionNav {
    data class Navigate(
        val route: String,
    ) : BreadcrumbOnCompositionNav

    data class PopToExclusive(
        val route: String,
    ) : BreadcrumbOnCompositionNav

    data class PopToItem(
        val itemId: UUID,
    ) : BreadcrumbOnCompositionNav
}
