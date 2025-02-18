package studio.lunabee.onesafe.feature.move.selectdestination

import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object SelectMoveDestination : OSDestination {
    private const val SelectDestinationPath: String = "selectDestination"
    const val DestinationItemIdArgument: String = "destinationItemId"

    override val route: String =
        "$SelectDestinationPath?$DestinationItemIdArgument={$DestinationItemIdArgument}"

    // null value is handle automatically
    fun getRoute(destinationItemId: UUID?): String = route.replace("{$DestinationItemIdArgument}", destinationItemId.toString())
}
