package studio.lunabee.onesafe.feature.itemactions

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import java.util.UUID

context(ComposeItemActionNavScope)
@Composable
fun ComposeItemAction(
    getSafeItemActionDelegate: GetSafeItemActionUiStateDelegate,
) {
    val navigationAction by getSafeItemActionDelegate.navigationAction.collectAsStateWithLifecycle(initialValue = null)
    val dialogState by getSafeItemActionDelegate.itemActionDialogState.collectAsStateWithLifecycle()
    val snackbarState by getSafeItemActionDelegate.itemActionSnackbarState.collectAsStateWithLifecycle(null)

    dialogState?.DefaultAlertDialog()
    snackbarState?.snackbarVisuals?.let { snackbarVisuals ->
        LaunchedEffect(snackbarVisuals) {
            showSnackbar(snackbarVisuals)
        }
    }
    navigationAction?.let {
        LaunchedEffect(it) {
            when (val action = it) {
                is QuickActionNavigation.Move -> {
                    navigateToMove(action.safeItemId)
                    action.consumeState()
                }
                is QuickActionNavigation.Share -> {
                    navigateToShare(action.safeItemId, action.includeChildren)
                    action.consumeState()
                }
                is QuickActionNavigation.SendViaBubbles -> {
                    navigateToSendViaBubbles(action.safeItemId, action.includeChildren)
                    action.consumeState()
                }
                QuickActionNavigation.NavigateBack -> navigateBack()
            }
        }
    }
}

interface ComposeItemActionNavScope {
    val showSnackbar: (visuals: SnackbarVisuals) -> Unit
    val navigateToMove: (itemId: UUID) -> Unit
    val navigateToShare: (itemId: UUID, includeChildren: Boolean) -> Unit
    val navigateToSendViaBubbles: (itemId: UUID, includeChildren: Boolean) -> Unit
    val navigateBack: () -> Unit
}
