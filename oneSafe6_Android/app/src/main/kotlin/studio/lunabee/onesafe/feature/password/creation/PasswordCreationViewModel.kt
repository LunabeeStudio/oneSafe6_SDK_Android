package studio.lunabee.onesafe.feature.password.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
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
import studio.lunabee.onesafe.domain.model.password.PasswordStrength
import studio.lunabee.onesafe.domain.usecase.EstimatePasswordStrengthUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.CreateMasterKeyResult
import studio.lunabee.onesafe.domain.usecase.onboarding.GenerateCryptoForNewSafeUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.ResetEditCryptoUseCase
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class PasswordCreationViewModel @Inject constructor(
    private val generateCryptoForNewSafeUseCase: GenerateCryptoForNewSafeUseCase,
    private val resetEditCryptoUseCase: ResetEditCryptoUseCase,
    private val estimatePasswordStrengthUseCase: EstimatePasswordStrengthUseCase,
    private val isPasswordCorrectUseCase: IsPasswordCorrectUseCase,
) : ViewModel() {

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val _uiState: MutableStateFlow<PasswordCreationUiState> = MutableStateFlow(
        PasswordCreationUiState.Idle(passwordStrength = null),
    )
    val uiState: StateFlow<PasswordCreationUiState> = _uiState.asStateFlow()

    private val loadingDelegate: LBLoadingVisibilityDelayDelegate = LBLoadingVisibilityDelayDelegate()

    private var isLoading: AtomicBoolean = AtomicBoolean(false)

    fun createMasterKey(password: String) {
        if (isLoading.getAndSet(true)) return
        loadingDelegate.delayShowLoading {
            if (isLoading.get()) {
                _uiState.value = PasswordCreationUiState.Loading
            }
        }
        viewModelScope.launch {
            val isPasswordCorrectResult = isPasswordCorrectUseCase(password.toCharArray())
            if (isPasswordCorrectResult is LBResult.Success) { // password is the same as before
                _uiState.value = PasswordCreationUiState.Error
            } else {
                val result = generateCryptoForNewSafeUseCase(password.toCharArray())
                when (result) {
                    is LBResult.Failure -> {
                        _dialogState.value = ErrorDialogState(
                            result.throwable,
                            actions = listOf(DialogAction.commonOk(::dismissDialog)),
                        )
                        loadingDelegate.delayHideLoading {
                            _uiState.value = PasswordCreationUiState.Idle(getPasswordStrength(password))
                        }
                    }
                    is LBResult.Success -> {
                        when (result.successData) {
                            CreateMasterKeyResult.Ok -> {
                                _uiState.value = PasswordCreationUiState.Success(::resetUiState)
                            }
                            CreateMasterKeyResult.AlreadyExist -> {
                                _uiState.value = PasswordCreationUiState.Error
                            }
                        }
                    }
                }
            }
            isLoading.set(false)
        }
    }

    fun resetCryptoCreation() {
        resetEditCryptoUseCase()
    }

    private fun dismissDialog() {
        _dialogState.value = null
    }

    fun resetUiState(password: String) {
        viewModelScope.launch {
            _uiState.value = PasswordCreationUiState.Idle(getPasswordStrength(password))
        }
    }

    private suspend fun getPasswordStrength(password: String): LbcTextSpec? {
        return password.takeUnless { it.isEmpty() }?.let { estimatePasswordStrengthUseCase(password).let(::label) }
    }

    private fun label(passwordStrength: PasswordStrength): LbcTextSpec? = when (passwordStrength) {
        PasswordStrength.VeryWeak -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_veryWeak)
        PasswordStrength.Weak -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_weak)
        PasswordStrength.Good -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_good)
        PasswordStrength.Strong -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_strong)
        PasswordStrength.VeryStrong -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_veryStrong)
        PasswordStrength.BulletProof -> LbcTextSpec.StringResource(OSString.passwordStrength_masterPassword_bulletProof)
        else -> null
    }
}
