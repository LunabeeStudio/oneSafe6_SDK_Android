package studio.lunabee.onesafe.feature.share.encrypt

import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object EncryptShareDestination : OSDestination {
    private const val Path: String = "encryptShare"
    const val ItemToShareIdArgument: String = "itemToShare"
    const val IncludeChildrenArgument: String = "includeChildren"

    override val route: String = "$Path/$ItemToShareIdArgument={$ItemToShareIdArgument}/$IncludeChildrenArgument={$IncludeChildrenArgument}"

    fun getRoute(itemId: UUID, includeChildren: Boolean): String =
        route
            .replace("{$ItemToShareIdArgument}", itemId.toString())
            .replace("{$IncludeChildrenArgument}", includeChildren.toString())
}
