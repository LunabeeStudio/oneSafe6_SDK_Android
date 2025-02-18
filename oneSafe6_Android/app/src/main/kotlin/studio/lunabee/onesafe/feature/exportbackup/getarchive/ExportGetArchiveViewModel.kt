package studio.lunabee.onesafe.feature.exportbackup.getarchive

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportGetArchiveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _exportGetArchiveState: MutableStateFlow<ExportGetArchiveUiState> =
        MutableStateFlow(value = ExportGetArchiveUiState.Idle)
    val exportGetArchiveState: StateFlow<ExportGetArchiveUiState> = _exportGetArchiveState.asStateFlow()

    val archiveFile: File? = savedStateHandle.get<String>(ExportGetArchiveDestination.ArgArchivePath)?.let {
        File(Uri.decode(it))
    }

    fun saveFile(destUri: Uri, context: Context) {
        archiveFile?.let {
            context.contentResolver.openOutputStream(destUri)?.let { outputStream ->
                outputStream.use { archiveFile.inputStream().use { it.copyTo(outputStream) } }
                _exportGetArchiveState.value = ExportGetArchiveUiState.Success(type = ExportGetArchiveUiState.Type.Save) {
                    _exportGetArchiveState.value = ExportGetArchiveUiState.Idle
                }
            } ?: emitError()
        } ?: emitError()
    }

    fun emitError() {
        _exportGetArchiveState.value = ExportGetArchiveUiState.Error(
            dialogState = object : DialogState {
                override val actions: List<DialogAction> = listOf(
                    DialogAction(text = LbcTextSpec.StringResource(OSString.common_no)) {
                        _exportGetArchiveState.value = ExportGetArchiveUiState.Idle
                    },
                    DialogAction(text = LbcTextSpec.StringResource(OSString.common_yes)) {
                        _exportGetArchiveState.value = ExportGetArchiveUiState.RestartExport {
                            _exportGetArchiveState.value = ExportGetArchiveUiState.Idle
                        }
                    },
                )
                override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.export_backup_error_fileNotFound_title)
                override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.export_backup_error_fileNotFound_description)
                override val dismiss: () -> Unit = { _exportGetArchiveState.value = ExportGetArchiveUiState.Idle }
                override val customContent:
                    @Composable()
                    (() -> Unit)? = null
            },
        )
    }
}
