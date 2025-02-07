package studio.lunabee.onesafe.feature.move

import studio.lunabee.onesafe.feature.snackbar.MoveSucceedSnackbarState
import java.util.UUID

interface MoveActionState {
    object Idle : MoveActionState

    data class NavigateToItem(
        val itemId: UUID?,
        val snackbarState: MoveSucceedSnackbarState,
    ) : MoveActionState

    data class Error(
        val throwable: Throwable?,
    ) : MoveActionState
}
