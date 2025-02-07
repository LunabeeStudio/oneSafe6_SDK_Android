package studio.lunabee.onesafe.feature.verifypassword.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class CheckPasswordViewModel @Inject constructor(
    private val isPasswordCorrectUseCase: IsPasswordCorrectUseCase,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<CheckPasswordUiState> = MutableStateFlow(CheckPasswordUiState.Idle)
    val uiState: StateFlow<CheckPasswordUiState> = _uiState.asStateFlow()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    fun checkPassword(password: String) {
        if (isLoading.getAndSet(true)) return
        if (isLoading.get()) {
            _uiState.value = CheckPasswordUiState.Loading
        }
        viewModelScope.launch {
            val result = isPasswordCorrectUseCase(password.toCharArray())
            when (result) {
                is LBResult.Failure -> {
                    val error = result.throwable
                    if ((error as? OSCryptoError)?.code == OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD) {
                        _uiState.value = CheckPasswordUiState.WrongPassword
                    } else {
                        _uiState.value = CheckPasswordUiState.Idle
                        _dialogState.value = ErrorDialogState(
                            error = error,
                            actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
                        )
                    }
                }
                is LBResult.Success -> {
                    setSecuritySettingUseCase.setLastPasswordVerification()
                    _uiState.value = CheckPasswordUiState.RightPassword
                }
            }
            isLoading.set(false)
        }
    }

    fun resetState() {
        _uiState.value = CheckPasswordUiState.Idle
    }
}
