package studio.lunabee.onesafe.feature.importbackup.selectfile

import android.content.Context
import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.dialog.DialogState

@Stable
interface MetadataReadState {
    /**
     * Used when archive has been successfully unzipped and metadata are correctly read.
     */
    object Success : MetadataReadState

    /**
     * Used when archive is in a newer version than supported but has been successfully unzipped.
     */
    object NotFullySupported : MetadataReadState

    /**
     * Used when archive extracting is in progress.
     */
    data class ExtractInProgress(
        val progress: Float,
    ) : MetadataReadState

    /**
     * Default state when user entering the screen. Wait for file selection.
     * After a [Success], value of state should be reset to [WaitingForExtract].
     */
    object WaitingForExtract : MetadataReadState

    /**
     * Used if an error occurred during extraction or metadata read process.
     */
    data class Error(
        val dialogState: DialogState,
    ) : MetadataReadState

    data class LaunchPicker(val resetState: () -> Unit) : MetadataReadState

    /**
     * Used when a file URI was passed as a nav argument, we can start an extract right away, no need for the user to manually
     * pick the file
     */
    class StartExtract(
        val extract: (Context) -> Unit,
    ) : MetadataReadState

    class ExitWithError(
        val error: LbcTextSpec,
    ) : MetadataReadState
}
