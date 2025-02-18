package studio.lunabee.onesafe.feature.exportbackup.getarchive

import android.net.Uri
import studio.lunabee.onesafe.commonui.OSDestination

object ExportGetArchiveDestination : OSDestination {
    const val ArgArchivePath: String = "ArgArchivePath"

    override val route: String = "exportGetArchive/{$ArgArchivePath}"

    fun getRoute(archivePath: String): String = route.replace("{$ArgArchivePath}", Uri.encode(archivePath))
}
