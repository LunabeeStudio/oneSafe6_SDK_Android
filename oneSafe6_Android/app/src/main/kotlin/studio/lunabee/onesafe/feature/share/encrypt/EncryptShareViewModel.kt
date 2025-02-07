package studio.lunabee.onesafe.feature.share.encrypt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.share.SharingData
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.importexport.usecase.ShareItemsUseCase
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EncryptShareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export) archiveDir: File,
    shareItemsUseCase: ShareItemsUseCase,
) : ViewModel() {

    private val itemId: UUID? = savedStateHandle.get<String>(EncryptShareDestination.ItemToShareIdArgument)?.let(UUID::fromString)
    private val includeChildren: Boolean = savedStateHandle.get<Boolean>(EncryptShareDestination.IncludeChildrenArgument) ?: false

    private val _uiState = MutableStateFlow<EncryptShareUIState>(EncryptShareUIState.Idle)
    val uiState: StateFlow<EncryptShareUIState> get() = _uiState.asStateFlow()

    init {
        if (itemId != null) {
            viewModelScope.launch {
                shareItemsUseCase(itemId, includeChildren, archiveDir).collect { result: LBFlowResult<SharingData> ->
                    when (result) {
                        is LBFlowResult.Loading -> {
                            _uiState.value = EncryptShareUIState.Encrypting(result.partialData?.itemsNbr ?: 0)
                        }
                        is LBFlowResult.Success -> {
                            _uiState.value = EncryptShareUIState.ReadyToShare(result.successData)
                        }
                        is LBFlowResult.Failure -> {
                            _uiState.value = EncryptShareUIState.Error
                        }
                    }
                }
            }
        } else {
            _uiState.value = EncryptShareUIState.Error
        }
    }
}
