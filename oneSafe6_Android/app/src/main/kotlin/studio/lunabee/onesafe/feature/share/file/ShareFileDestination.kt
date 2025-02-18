package studio.lunabee.onesafe.feature.share.file

import android.net.Uri
import studio.lunabee.onesafe.domain.model.share.SharingData
import studio.lunabee.onesafe.commonui.OSDestination

object ShareFileDestination : OSDestination {
    private const val Path: String = "shareFile"
    const val PasswordArg: String = "password"
    const val ItemsNbrArg: String = "fileNbr"
    const val FilePathArgs: String = "filePath"

    override val route: String = "$Path/$PasswordArg={$PasswordArg}/$ItemsNbrArg={$ItemsNbrArg}/$FilePathArgs={$FilePathArgs}"

    fun getRoute(sharingData: SharingData): String =
        route
            // We need to encode it twice due to the special characters in the password
            .replace("{$PasswordArg}", Uri.encode(Uri.encode(sharingData.password.orEmpty())))
            .replace("{$ItemsNbrArg}", sharingData.itemsNbr.toString())
            .replace("{$FilePathArgs}", Uri.encode(sharingData.file?.absolutePath.orEmpty()))
}
