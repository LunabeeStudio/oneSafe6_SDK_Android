package studio.lunabee.onesafe.feature.exportbackup.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.importexport.ExportMetadata
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import studio.lunabee.onesafe.importexport.usecase.GetMetadataForExportUseCase
import javax.inject.Inject

@HiltViewModel
class ExportAuthViewModel @Inject constructor(
    getMetadataForExportUseCase: GetMetadataForExportUseCase,
    private val isPasswordCorrectUseCase: IsPasswordCorrectUseCase,
) : ViewModel() {
    private val _exportAuthState: MutableStateFlow<ExportAuthUiState> =
        MutableStateFlow(ExportAuthUiState.WaitForPassword)
    val exportAuthState: StateFlow<ExportAuthUiState> = _exportAuthState.asStateFlow()

    private val _exportMetadata: MutableStateFlow<ExportMetadata?> = MutableStateFlow(
        ExportMetadata(
            itemCount = 0,
            contactCount = 0,
        ),
    )
    val exportMetadata: StateFlow<ExportMetadata?> = _exportMetadata.asStateFlow()

    init {
        viewModelScope.launch {
            _exportMetadata.value = when (val result = getMetadataForExportUseCase()) {
                is LBResult.Failure -> null
                is LBResult.Success -> result.successData
            }
        }
    }

    fun checkPassword(password: String) {
        viewModelScope.launch {
            _exportAuthState.value = ExportAuthUiState.CheckingPassword
            val result = isPasswordCorrectUseCase(
                password = password.toCharArray(),
            )
            _exportAuthState.value = when (result) {
                is LBResult.Failure -> ExportAuthUiState.PasswordIncorrect {
                    _exportAuthState.value = ExportAuthUiState.WaitForPassword
                }
                is LBResult.Success -> ExportAuthUiState.PasswordValid {
                    _exportAuthState.value = ExportAuthUiState.WaitForPassword
                }
            }
        }
    }
}
