package studio.lunabee.onesafe.feature.migration.savedata

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegate
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegateImpl
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportOverrideAlertDialogState
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.feature.migration.MigrationManager
import javax.inject.Inject

@HiltViewModel
class MigrationSaveDataViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    countSafeItemInParentUseCase: CountSafeItemUseCase,
    private val importSaveDataMetaDataDelegateImpl: ImportGetMetaDataDelegateImpl,
    private val migrationManager: MigrationManager,
) : ViewModel(), ImportGetMetaDataDelegate by importSaveDataMetaDataDelegateImpl {
    private val loadingDelegate: LBLoadingVisibilityDelayDelegate = LBLoadingVisibilityDelayDelegate()

    private val _importSaveDataState: MutableStateFlow<ImportSaveDataUiState> =
        MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
    val importSaveDataState: StateFlow<ImportSaveDataUiState> = _importSaveDataState.asStateFlow()

    private val _currentSafeItemCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSafeItemCount: StateFlow<Int> = _currentSafeItemCount.asStateFlow()

    init {
        viewModelScope.launch {
            val result = countSafeItemInParentUseCase.notDeleted(null)
            when (result) {
                is LBResult.Failure -> _importSaveDataState.value = getErrorDialogState(result.throwable)
                is LBResult.Success -> _currentSafeItemCount.value = result.successData
            }
        }
    }

    private val archiveUri = savedStateHandle.get<String>(MigrationSaveDataDestination.uriArgument)!!.let(Uri::parse)

    private var selectedMode: ImportMode? = null
    fun setSelectedMode(importMode: ImportMode?) {
        selectedMode = importMode
    }

    fun startMigration() {
        when (selectedMode) {
            ImportMode.AppendInFolder -> startMigration(ImportMode.AppendInFolder)
            ImportMode.Replace -> {
                if (currentSafeItemCount.value > 0) {
                    _importSaveDataState.value = ImportSaveDataUiState.Dialog(
                        ImportOverrideAlertDialogState(
                            launchImport = { startMigration(ImportMode.Replace) },
                            dismiss = { _importSaveDataState.value = ImportSaveDataUiState.WaitingForUserChoice },
                        ),
                    )
                } else {
                    startMigration(ImportMode.Replace)
                }
            }
            ImportMode.Append -> {
                /* no-op */
            }
            null -> _importSaveDataState.value = getErrorDialogState(OSAppError(OSAppError.Code.MIGRATION_IMPORT_MODE_NOT_SET))
        }
    }

    private fun startMigration(selectedMode: ImportMode) {
        migrationManager.initMigration(
            onResult = { result ->
                when (result) {
                    is LBResult.Failure -> _importSaveDataState.value = getErrorDialogState(result.throwable)
                    is LBResult.Success -> launchMigration(selectedMode, archiveUri)
                }
            },
        )
    }

    private fun launchMigration(importMode: ImportMode, archiveUri: Uri) {
        migrationManager.getMigrationFlow(importMode, archiveUri).onEach { importResult ->
            when (importResult) {
                is LBFlowResult.Failure -> _importSaveDataState.value = getErrorDialogState(importResult.throwable)
                is LBFlowResult.Loading -> loadingDelegate.delayShowLoading {
                    _importSaveDataState.value = ImportSaveDataUiState.ImportInProgress(importResult.progress ?: .0f)
                }
                is LBFlowResult.Success -> loadingDelegate.delayHideLoading {
                    _importSaveDataState.value = ImportSaveDataUiState.Success
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getErrorDialogState(err: Throwable?) = ImportSaveDataUiState.Dialog(
        dialogState = ErrorDialogState(
            error = err,
            actions = listOf(
                DialogAction(
                    text = LbcTextSpec.StringResource(id = OSString.common_retry),
                    type = DialogAction.Type.Normal,
                    onClick = {
                        resetState()
                    },
                ),
            ),
            dismiss = {
                resetState()
            },
        ),
    )

    private fun resetState() {
        _importSaveDataState.value = ImportSaveDataUiState.WaitingForUserChoice
        selectedMode = null
    }
}
