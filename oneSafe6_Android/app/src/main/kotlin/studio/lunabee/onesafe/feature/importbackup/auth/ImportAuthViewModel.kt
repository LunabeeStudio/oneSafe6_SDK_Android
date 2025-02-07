package studio.lunabee.onesafe.feature.importbackup.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegate
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegateImpl
import studio.lunabee.onesafe.importexport.usecase.ImportAuthUseCase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ImportAuthViewModel @Inject constructor(
    private val importAuthUseCase: ImportAuthUseCase,
    private val importSaveDataMetaDataDelegateImpl: ImportGetMetaDataDelegateImpl,
) : ViewModel(), ImportGetMetaDataDelegate by importSaveDataMetaDataDelegateImpl {

    var creationDate: LocalDateTime? = null
        private set

    private val _importAuthState: MutableStateFlow<ImportAuthState> =
        MutableStateFlow(value = ImportAuthState.WaitingForUserInput)
    val importAuthState: StateFlow<ImportAuthState> = _importAuthState.asStateFlow()

    var archiveKind: OSArchiveKind? = null
        private set

    init {
        when (metadataResult) {
            is LBResult.Success -> {
                archiveKind = metadataResult.successData.archiveKind
                creationDate = LocalDateTime.parse(metadataResult.successData.createdAt.toString(), DateTimeFormatter.ISO_DATE_TIME)
            }
            is LBResult.Failure -> {
                handleError(metadataResult.throwable)
            }
        }
    }

    fun unlockArchive(password: String) {
        viewModelScope.launch {
            importAuthUseCase(
                password = password.toCharArray(),
            ).collect { result ->
                when (result) {
                    is LBFlowResult.Failure -> handleError(result.throwable)
                    is LBFlowResult.Loading -> _importAuthState.value = ImportAuthState.AuthInProgress
                    is LBFlowResult.Success -> _importAuthState.value = ImportAuthState.Success(
                        reset = { _importAuthState.value = ImportAuthState.WaitingForUserInput },
                        doesArchiveContainsBubblesData = (metadataResult.data?.bubblesContactCount ?: 0) != 0,
                    )
                }
            }
        }
    }

    fun waitForUserInput() {
        _importAuthState.value = ImportAuthState.WaitingForUserInput
    }

    private fun handleError(throwable: Throwable?) {
        when ((throwable as? OSImportExportError)?.code) {
            OSImportExportError.Code.WRONG_CREDENTIALS -> _importAuthState.value = ImportAuthState.WrongCredentials
            else -> emitUnexpectedError(throwable = throwable)
        }
    }

    private fun emitUnexpectedError(throwable: Throwable?) {
        _importAuthState.value = ImportAuthState.UnexpectedError(
            dialogState = ErrorDialogState(
                error = throwable,
                actions = listOf(DialogAction.commonOk(::dismissDialog)),
                dismiss = ::dismissDialog,
            ),
        )
    }

    private fun dismissDialog() {
        _importAuthState.value = ImportAuthState.WaitingForUserInput
    }

    val importAuthArchiveKindLabels: ImportAuthArchiveKindLabels
        get() = if (archiveKind == OSArchiveKind.Sharing) {
            ImportAuthArchiveKindLabels.Share
        } else {
            ImportAuthArchiveKindLabels.Backup(creationDate)
        }
}
