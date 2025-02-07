package studio.lunabee.onesafe.feature.importbackup.savedata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.feature.importbackup.savedelegate.ImportSaveDataDelegate
import studio.lunabee.onesafe.feature.importbackup.savedelegate.ImportSaveDataDelegateImpl
import javax.inject.Inject

@HiltViewModel
class ImportSaveDataViewModel @Inject constructor(
    countSafeItemInParentUseCase: CountSafeItemUseCase,
    private val importGetMetaDataDelegateImpl: ImportGetMetaDataDelegateImpl,
    private val importSaveDataDelegate: ImportSaveDataDelegateImpl,
) : ViewModel(),
    ImportGetMetaDataDelegate by importGetMetaDataDelegateImpl,
    ImportSaveDataDelegate by importSaveDataDelegate {
    private val _currentSafeItemCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSafeItemCount: StateFlow<Int> = _currentSafeItemCount.asStateFlow()
    var archiveKind: OSArchiveKind = OSArchiveKind.Unknown

    init {
        archiveKind = metadataResult.data?.archiveKind ?: OSArchiveKind.Unknown
        if (metadataResult is LBResult.Success && metadataResult.successData.archiveKind == OSArchiveKind.Sharing) {
            startImport(ImportMode.Append)
        } else if (metadataResult is LBResult.Failure) {
            setState(ImportSaveDataUiState.ExitWithError(null))
        }

        viewModelScope.launch {
            val result = countSafeItemInParentUseCase.notDeleted(null)
            when (result) {
                is LBResult.Failure -> {
                    setState(
                        ImportSaveDataUiState.ExitWithError(
                            ErrorSnackbarState(error = result.throwable, onClick = {}),
                        ),
                    )
                }
                is LBResult.Success -> {
                    _currentSafeItemCount.value = result.successData
                }
            }
        }
    }

    fun startImport(mode: ImportMode) {
        when (mode) {
            ImportMode.AppendInFolder, ImportMode.Append -> launchImport(mode = mode)
            ImportMode.Replace -> {
                if (currentSafeItemCount.value > 0) {
                    val state = ImportSaveDataUiState.Dialog(
                        ImportOverrideAlertDialogState(
                            launchImport = { launchImport(ImportMode.Replace) },
                            dismiss = { setState(ImportSaveDataUiState.WaitingForUserChoice) },
                        ),
                    )
                    setState(state)
                } else {
                    launchImport(mode = ImportMode.Replace)
                }
            }
        }
    }
}
