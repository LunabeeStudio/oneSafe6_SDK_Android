package studio.lunabee.onesafe.feature.importbackup.auth

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
interface ImportAuthState {
    data class Success(
        val reset: () -> Unit,
        val doesArchiveContainsBubblesData: Boolean,
    ) : ImportAuthState

    object AuthInProgress : ImportAuthState

    object WaitingForUserInput : ImportAuthState

    data class UnexpectedError(
        val dialogState: DialogState,
    ) : ImportAuthState

    object WrongCredentials : ImportAuthState
}
