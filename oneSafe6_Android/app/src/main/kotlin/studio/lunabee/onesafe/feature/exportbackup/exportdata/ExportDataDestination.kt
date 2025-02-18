package studio.lunabee.onesafe.feature.exportbackup.exportdata

import studio.lunabee.onesafe.commonui.OSDestination

object ExportDataDestination : OSDestination {
    const val ArgItemCount: String = "ArgItemCount"
    const val ArgContactCount: String = "ArgContactCount"
    override val route: String = "exportData/{$ArgItemCount}/{$ArgContactCount}"

    fun getRoute(itemCount: Int, contactCount: Int): String = route
        .replace("{$ArgItemCount}", itemCount.toString())
        .replace("{$ArgContactCount}", contactCount.toString())
}
