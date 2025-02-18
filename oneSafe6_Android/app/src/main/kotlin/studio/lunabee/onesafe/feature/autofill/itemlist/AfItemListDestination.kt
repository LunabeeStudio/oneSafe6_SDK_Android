package studio.lunabee.onesafe.feature.autofill.itemlist

import studio.lunabee.onesafe.commonui.OSDestination

object AfItemListDestination : OSDestination {
    const val ClientDomainArgs: String = "ClientDomainArgs"
    const val ClientPackageArgs: String = "ClientPackageArgs"
    override val route: String = "autofill_itemsList?$ClientDomainArgs={$ClientDomainArgs}&$ClientPackageArgs={$ClientPackageArgs}"

    fun getRoute(clientDomain: String, clientPackage: String): String = route
        .replace("{$ClientDomainArgs}", clientDomain)
        .replace("{$ClientPackageArgs}", clientPackage)
}
