package studio.lunabee.onesafe.help.cipherkeyprompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.domain.usecase.authentication.SetDatabaseKeyUseCase
import javax.inject.Inject

@HiltViewModel
class CipherKeyPromptViewModel @Inject constructor(
    private val loadingManager: LoadingManager,
    private val setDatabaseKeyUseCase: SetDatabaseKeyUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<CipherKeyPromptUiState> = MutableStateFlow(CipherKeyPromptUiState.default())
    val uiState: StateFlow<CipherKeyPromptUiState> = _uiState.asStateFlow()

    fun setKey(password: String) {
        _uiState.value = _uiState.value.copy(
            key = password,
            openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Idle,
        )
    }

    fun confirm() {
        viewModelScope.launch {
            loadingManager.startLoading()
            val result = setDatabaseKeyUseCase(uiState.value.key)
            when (result) {
                is LBResult.Failure -> {
                    loadingManager.stopLoading()
                    _uiState.value = _uiState.value.copy(
                        openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Error(result.throwable.description()),
                    )
                }
                is LBResult.Success ->
                    // Don't stop loading, let the app restart
                    _uiState.value = _uiState.value.copy(openDatabaseResult = CipherKeyPromptUiState.OpenDatabaseState.Success)
            }
        }
    }
}
