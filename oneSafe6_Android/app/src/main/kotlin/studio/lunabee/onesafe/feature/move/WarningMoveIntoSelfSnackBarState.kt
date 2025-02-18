package studio.lunabee.onesafe.feature.move

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

object WarningMoveIntoSelfSnackBarState : SnackbarState() {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.move_selectDestination_moveIntoItself_warning)
}
