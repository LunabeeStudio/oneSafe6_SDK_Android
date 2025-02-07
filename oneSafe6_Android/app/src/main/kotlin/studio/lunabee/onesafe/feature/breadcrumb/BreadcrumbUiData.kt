package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.runtime.Immutable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSNameProvider
import java.util.UUID

@Immutable
interface BreadcrumbUiDataSpec {
    val destination: BreadcrumbDestinationSpec
    val name: LbcTextSpec
    val mainAction: BreadcrumbMainAction
}

class ItemBreadcrumbUiData(
    itemId: UUID,
    private val nameProvider: OSNameProvider,
    override val mainAction: BreadcrumbMainAction,
) : BreadcrumbUiDataSpec {
    override val destination: BreadcrumbDestinationSpec = ItemBreadcrumbDestination(itemId)
    override val name: LbcTextSpec
        get() = nameProvider.name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemBreadcrumbUiData

        if (nameProvider != other.nameProvider) return false
        if (mainAction != other.mainAction) return false
        if (destination != other.destination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameProvider.hashCode()
        result = 31 * result + mainAction.hashCode()
        result = 31 * result + destination.hashCode()
        return result
    }
}

class RouteBreadcrumbUiData private constructor(
    override val destination: HardBreadcrumbDestination,
    override val name: LbcTextSpec,
    override val mainAction: BreadcrumbMainAction,
) : BreadcrumbUiDataSpec {

    companion object {
        fun home(): RouteBreadcrumbUiData = RouteBreadcrumbUiData(
            destination = HardBreadcrumbDestination.Home,
            name = LbcTextSpec.StringResource(OSString.common_home),
            mainAction = BreadcrumbMainAction.AddItem,
        )

        fun bin(): RouteBreadcrumbUiData = RouteBreadcrumbUiData(
            destination = HardBreadcrumbDestination.Bin,
            name = LbcTextSpec.StringResource(OSString.common_bin),
            mainAction = BreadcrumbMainAction.None,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteBreadcrumbUiData

        if (destination != other.destination) return false
        if (name != other.name) return false
        if (mainAction != other.mainAction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + mainAction.hashCode()
        return result
    }
}
