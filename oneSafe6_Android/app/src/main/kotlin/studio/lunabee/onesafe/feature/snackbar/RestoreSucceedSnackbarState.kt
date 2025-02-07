package studio.lunabee.onesafe.feature.snackbar

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarAction
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import java.util.UUID

class RestoreSucceedSnackbarState(
    restoredItemId: UUID,
    onDismiss: () -> Unit,
) : SnackbarState(
    SnackbarAction.Navigation(
        actionLabel = LbcTextSpec.StringResource(OSString.common_see),
        route = ItemDetailsDestination.getRoute(restoredItemId),
        onDismiss = onDismiss,
    ),
) {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.restoreFeedback_successMessage)
}
