package studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword

import androidx.lifecycle.SavedStateHandle
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
import studio.lunabee.onesafe.domain.usecase.autodestruction.DeriveAutoDestructionPasswordUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.EnableAutoDestructionUseCase
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.jvm.get
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class AutoDestructionPasswordConfirmationViewModel @Inject constructor(
    private val enableAutoDestructionUseCase: EnableAutoDestructionUseCase,
    private val deriveAutoDestructionPasswordUseCase: DeriveAutoDestructionPasswordUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialPasswordHash: String = savedStateHandle.get<String>(AutoDestructionPasswordConfirmationDestination.PasswordHashArg)
        ?: error("missing Arg")

    private val salt: String = savedStateHandle.get<String>(AutoDestructionPasswordConfirmationDestination.SaltArg)
        ?: error("missing Arg")

    private val _uiState: MutableStateFlow<AutoDestructionPasswordConfirmationUiState> =
        MutableStateFlow(AutoDestructionPasswordConfirmationUiState.Idle)
    val uiState: StateFlow<AutoDestructionPasswordConfirmationUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    fun checkPassword(password: String) {
        viewModelScope.launch {
            if (deriveAutoDestructionPasswordUseCase(password, salt) == initialPasswordHash) {
                setupAutoDestruction(password)
            } else {
                _uiState.value = AutoDestructionPasswordConfirmationUiState.FieldError(
                    OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD.get(),
                    ::resetUiState,
                )
            }
            isLoading.set(false)
        }
    }

    private suspend fun setupAutoDestruction(password: String) {
        val result = enableAutoDestructionUseCase(password.toCharArray())
        when (result) {
            is LBResult.Failure -> {
                val error = result.throwable
                _uiState.value = AutoDestructionPasswordConfirmationUiState.Idle
                _dialogState.value = ErrorDialogState(
                    error = error,
                    actions = listOf(
                        DialogAction.commonOk {
                            _dialogState.value = null
                            _uiState.value = AutoDestructionPasswordConfirmationUiState.Exit(null)
                        },
                    ),
                )
            }
            is LBResult.Success -> {
                _uiState.value = AutoDestructionPasswordConfirmationUiState.Exit(
                    MessageSnackBarState(LbcTextSpec.StringResource(OSString.autodestruction_success)),
                )
            }
        }
    }

    private fun resetUiState() {
        _uiState.value = AutoDestructionPasswordConfirmationUiState.Idle
    }
}
