package studio.lunabee.onesafe.feature.itemform.screen

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarAction
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

class CaptureAnotherSnackbarState(takeAnotherPhoto: () -> Unit) : SnackbarState(
    snackbarAction = SnackbarAction.Default(
        onClick = takeAnotherPhoto,
        actionLabel = LbcTextSpec.StringResource(OSString.common_yes),
        onDismiss = {},
    ),
) {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_snackbar_anotherPhoto)
}
