package studio.lunabee.onesafe.feature.snackbar

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

class AddToFavoriteSuccessSnackbarState(
    itemOSNameProvider: OSNameProvider,
) : SnackbarState() {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(
        OSString.setFavorite_success_message,
        itemOSNameProvider.truncatedName,
    )
}
