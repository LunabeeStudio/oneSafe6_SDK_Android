package studio.lunabee.onesafe.feature.password.confirmation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
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
class OnboardingPasswordConfirmationViewModel @Inject constructor(
    private val confirmPasswordUseCase: ConfirmPasswordUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<OnboardingPasswordConfirmationUiState> = MutableStateFlow(
        OnboardingPasswordConfirmationUiState.Idle,
    )
    val uiState: StateFlow<OnboardingPasswordConfirmationUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val loadingDelegate: LBLoadingVisibilityDelayDelegate = LBLoadingVisibilityDelayDelegate()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    fun checkPassword(password: String) {
        if (isLoading.getAndSet(true)) return
        loadingDelegate.delayShowLoading {
            if (isLoading.get()) {
                _uiState.value = OnboardingPasswordConfirmationUiState.Loading
            }
        }
        viewModelScope.launch {
            val result = confirmPasswordUseCase(password.toCharArray())
            when (result) {
                is LBResult.Failure -> {
                    loadingDelegate.delayHideLoading {
                        val error = result.throwable
                        if ((error as? OSDomainError)?.code == OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD) {
                            _uiState.value = OnboardingPasswordConfirmationUiState.FieldError(error, ::resetUiState)
                        } else {
                            _uiState.value = OnboardingPasswordConfirmationUiState.Idle
                            _dialogState.value = ErrorDialogState(
                                error = error,
                                actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
                            )
                        }
                    }
                }
                is LBResult.Success -> {
                    _uiState.value = OnboardingPasswordConfirmationUiState.Success(::resetUiState)
                }
            }
            isLoading.set(false)
        }
    }

    private fun resetUiState() {
        _uiState.value = OnboardingPasswordConfirmationUiState.Idle
    }
}
