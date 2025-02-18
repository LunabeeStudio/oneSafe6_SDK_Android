package studio.lunabee.onesafe.feature.verifypassword.wrongpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import javax.inject.Inject

@HiltViewModel
class WrongPasswordViewModel @Inject constructor(private val getBiometricCipherUseCase: GetBiometricCipherUseCase) : ViewModel() {

    private val _uiState: MutableStateFlow<WrongPasswordUiState> = MutableStateFlow(WrongPasswordUiState.Idle)
    val uiState: StateFlow<WrongPasswordUiState> = _uiState.asStateFlow()

    fun startChangePasswordFlow() {
        viewModelScope.launch {
            val cipher = getBiometricCipherUseCase.forVerify().data
            if (cipher != null) {
                _uiState.value = WrongPasswordUiState.ShowBiometric(cipher)
            }
        }
    }

    fun resetState() {
        _uiState.value = WrongPasswordUiState.Idle
    }
}
