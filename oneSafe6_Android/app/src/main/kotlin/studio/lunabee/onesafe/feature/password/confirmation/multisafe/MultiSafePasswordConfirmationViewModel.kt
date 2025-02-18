package studio.lunabee.onesafe.feature.password.confirmation.multisafe

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
import studio.lunabee.onesafe.domain.usecase.onboarding.ConfirmPasswordUseCase
import studio.lunabee.onesafe.error.OSDomainError
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MultiSafePasswordConfirmationViewModel @Inject constructor(
    private val confirmPasswordUseCase: ConfirmPasswordUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<MultiSafePasswordConfirmationUiState> = MutableStateFlow(
        MultiSafePasswordConfirmationUiState.Idle,
    )
    val uiState: StateFlow<MultiSafePasswordConfirmationUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    fun checkPassword(password: String) {
        if (isLoading.getAndSet(true)) return
        if (isLoading.get()) {
            _uiState.value = MultiSafePasswordConfirmationUiState.Loading
        }
        viewModelScope.launch {
            val result = confirmPasswordUseCase(password.toCharArray())
            when (result) {
                is LBResult.Failure -> {
                    val error = result.throwable
                    if ((error as? OSDomainError)?.code == OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD) {
                        _uiState.value = MultiSafePasswordConfirmationUiState.FieldError(error, ::resetUiState)
                    } else {
                        _uiState.value = MultiSafePasswordConfirmationUiState.Idle
                        _dialogState.value = ErrorDialogState(
                            error = error,
                            actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
                        )
                    }
                }
                is LBResult.Success -> {
                    _uiState.value = MultiSafePasswordConfirmationUiState.Success(::resetUiState)
                }
            }
            isLoading.set(false)
        }
    }

    private fun resetUiState() {
        _uiState.value = MultiSafePasswordConfirmationUiState.Idle
    }
}
