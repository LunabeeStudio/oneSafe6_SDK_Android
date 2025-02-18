package studio.lunabee.onesafe.feature.snackbar

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarAction
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import java.util.UUID

class DuplicateSucceedSnackbarState(
    duplicatedItemId: UUID,
    onDismiss: () -> Unit,
) : SnackbarState(
    SnackbarAction.Navigation(
        actionLabel = LbcTextSpec.StringResource(OSString.safeItemDetail_duplicateFeedback_successAction),
        route = ItemDetailsDestination.getRoute(duplicatedItemId),
        onDismiss = onDismiss,
    ),
) {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(
        OSString.safeItemDetail_duplicateFeedback_successMessage,
    )
}
