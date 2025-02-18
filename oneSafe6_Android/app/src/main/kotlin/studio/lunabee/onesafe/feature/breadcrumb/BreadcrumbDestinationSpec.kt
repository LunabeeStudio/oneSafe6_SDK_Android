package studio.lunabee.onesafe.feature.breadcrumb

import android.os.Bundle
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import studio.lunabee.onesafe.feature.bin.BinDestination
import studio.lunabee.onesafe.feature.home.HomeDestination
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import java.util.UUID

@Immutable
sealed interface BreadcrumbDestinationSpec {
    companion object {
        fun fromRoute(route: String, arguments: Bundle?): BreadcrumbDestinationSpec? =
            when (route) {
                ItemDetailsDestination.route -> {
                    val itemId = arguments?.getString(ItemDetailsDestination.itemIdArgument)!!.let(UUID::fromString)
                    ItemBreadcrumbDestination(itemId)
                }
                else -> HardBreadcrumbDestination.entries.firstOrNull { destination ->
                    destination.route == route
                }
            }
    }
}

data class ItemBreadcrumbDestination(
    val itemId: UUID,
) : BreadcrumbDestinationSpec

@Immutable
enum class HardBreadcrumbDestination(val route: String) : BreadcrumbDestinationSpec {
    Home(HomeDestination.route),
    Bin(BinDestination.route),
    ;

    val breadcrumbUiDataPath: ImmutableList<BreadcrumbUiDataSpec>
        get() = when (this) {
            Home -> persistentListOf(RouteBreadcrumbUiData.home())
            Bin -> persistentListOf(RouteBreadcrumbUiData.home(), RouteBreadcrumbUiData.bin())
        }
}
