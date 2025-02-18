package studio.lunabee.onesafe.feature.autofill.creation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object AfItemCreationDestination : OSDestination {
    const val ItemTypeArg: String = "itemTypeArgument"
    const val ItemParentIdArg: String = "ItemParentIdArgument"
    const val ItemParentColorArg: String = "ItemParentColorArg"
    const val IdentiferArgs: String = "identifierArg"
    const val PasswordArgs: String = "passwordArg"
    const val ClientDomainArgs: String = "clientDomain"
    const val ClientPackageArgs: String = "clientPackage"

    override val route: String = "AutoFillItemCreation/{$ItemTypeArg}?" +
        "$ItemParentIdArg={$ItemParentIdArg}" +
        "&$ItemParentColorArg={$ItemParentColorArg}" +
        "&$IdentiferArgs={$IdentiferArgs}" +
        "&$PasswordArgs={$PasswordArgs}" +
        "&$ClientDomainArgs={$ClientDomainArgs}" +
        "&$ClientPackageArgs={$ClientPackageArgs}"

    fun getRoute(
        itemType: String,
        itemParentId: UUID? = null,
        color: Color? = null,
        identifier: String? = null,
        password: String? = null,
        clientDomain: String? = null,
        clientPackage: String? = null,
    ): String {
        var route = this.route.replace("{$ItemTypeArg}", itemType)
        if (itemParentId != null) {
            route = route.replace("{$ItemParentIdArg}", itemParentId.toString())
        }
        if (color != null) {
            route = route.replace("{$ItemParentColorArg}", color.toArgb().toString())
        }
        if (identifier != null) {
            route = route.replace("{$IdentiferArgs}", identifier)
        }
        if (password != null) {
            route = route.replace("{$PasswordArgs}", password)
        }
        if (clientDomain != null) {
            route = route.replace("{$ClientDomainArgs}", clientDomain)
        }
        if (clientPackage != null) {
            route = route.replace("{$ClientPackageArgs}", clientPackage)
        }
        return route
    }
}
