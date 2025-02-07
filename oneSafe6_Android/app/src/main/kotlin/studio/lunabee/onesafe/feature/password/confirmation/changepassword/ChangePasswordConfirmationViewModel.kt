package studio.lunabee.onesafe.feature.password.confirmation.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.domain.usecase.authentication.ChangePasswordUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.DisableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.ConfirmPasswordUseCase
import studio.lunabee.onesafe.error.OSDomainError
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChangePasswordConfirmationViewModel @Inject constructor(
    private val confirmPasswordUseCase: ConfirmPasswordUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChangePasswordConfirmationUiState> =
        MutableStateFlow(ChangePasswordConfirmationUiState.Idle)
    val uiState: StateFlow<ChangePasswordConfirmationUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    fun checkPassword(password: String) {
        if (isLoading.getAndSet(true)) return
        if (isLoading.get()) {
            _uiState.value = ChangePasswordConfirmationUiState.Loading
        }
        viewModelScope.launch {
            val result = confirmPasswordUseCase(password.toCharArray())
            when (result) {
                is LBResult.Failure -> {
                    val error = result.throwable
                    if ((error as? OSDomainError)?.code == OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD) {
                        _uiState.value = ChangePasswordConfirmationUiState.FieldError(error, ::resetUiState)
                    } else {
                        _uiState.value = ChangePasswordConfirmationUiState.Idle
                        _dialogState.value = ErrorDialogState(
                            error = error,
                            actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
                        )
                    }
                }
                is LBResult.Success -> {
                    changePassword(password)
                }
            }
            isLoading.set(false)
        }
    }

    private suspend fun changePassword(password: String) {
        val isBiometricEnabled = isCurrentSafeBiometricEnabledUseCase()
        val result = changePasswordUseCase(password.toCharArray())
        when (result) {
            is LBResult.Failure -> {
                val error = result.throwable
                _uiState.value = ChangePasswordConfirmationUiState.Idle
                _dialogState.value = ErrorDialogState(
                    error = error,
                    actions = listOf(
                        DialogAction.commonOk {
                            _dialogState.value = null
                            _uiState.value = ChangePasswordConfirmationUiState.Exit(null, false)
                        },
                    ),
                )
            }
            is LBResult.Success -> {
                if (isBiometricEnabled) {
                    disableBiometricUseCase()
                }
                _uiState.value = ChangePasswordConfirmationUiState.Exit(
                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.settings_section_changePassword_feedback)),
                    isBiometricEnabled,
                )
            }
        }
    }

    private fun resetUiState() {
        _uiState.value = ChangePasswordConfirmationUiState.Idle
    }
}
