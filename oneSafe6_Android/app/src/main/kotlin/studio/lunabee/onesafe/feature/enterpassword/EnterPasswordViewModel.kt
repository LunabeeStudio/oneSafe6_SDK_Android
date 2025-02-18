package studio.lunabee.onesafe.feature.enterpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import javax.inject.Inject

@HiltViewModel
class EnterPasswordViewModel @Inject constructor(
    private val isPasswordCorrectUseCase: IsPasswordCorrectUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<EnterPasswordUiState> = MutableStateFlow(EnterPasswordUiState())
    val uiState: StateFlow<EnterPasswordUiState> = _uiState.asStateFlow()

    fun checkPasswordIsCorrect(password: String) {
        _uiState.value = uiState.value.copy(screenResult = EnterPasswordScreenResultState.Loading)
        viewModelScope.launch {
            if (isPasswordCorrectUseCase(password.toCharArray()) is LBResult.Success) {
                _uiState.value = uiState.value.copy(screenResult = EnterPasswordScreenResultState.Success)
            } else {
                _uiState.value = uiState.value.copy(screenResult = EnterPasswordScreenResultState.Error)
            }
        }
    }

    fun resetScreenResultState() {
        _uiState.value = uiState.value.copy(screenResult = EnterPasswordScreenResultState.Idle)
    }

    fun setPassword(value: String) {
        _uiState.value = uiState.value.copy(password = value)
    }
}
