package studio.lunabee.onesafe.feature.snackbar

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarAction
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

class SeeBubblesConversationSnackbar(
    val onClick: () -> Unit,
) : SnackbarState(
    SnackbarAction.Default(
        actionLabel = LbcTextSpec.StringResource(OSString.common_see),
        onClick = onClick,
        onDismiss = {},
    ),
) {
    override val message: LbcTextSpec =
        LbcTextSpec.StringResource(OSString.bubbles_decryptMessage_archive_messageAdded)
}
