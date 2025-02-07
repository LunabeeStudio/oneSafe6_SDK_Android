package studio.lunabee.onesafe.feature.biometric

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.authentication.DisableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.EnableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.InitializeEditBiometricUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.onboarding.SignUpUiState
import javax.crypto.Cipher
import javax.inject.Inject

abstract class BiometricCreationViewModel(
    private val doBiometricUseCase: suspend (cipher: Cipher) -> LBResult<Unit>,
    private val disableBiometricUseCase: DisableBiometricUseCase,
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
) : ViewModel() {
    var state: SignUpUiState by mutableStateOf(SignUpUiState.Idle)
    fun getBiometricCipher(): Cipher? = getBiometricCipherUseCase.forCreate().data

    fun disableBiometric() {
        viewModelScope.launch {
            disableBiometricUseCase()
        }
    }

    fun enableBiometric(biometricCipher: Cipher) {
        viewModelScope.launch {
            val result = doBiometricUseCase(biometricCipher)
            state = when (result) {
                is LBResult.Success<Unit> -> SignUpUiState.Success
                is LBResult.Failure<Unit> -> SignUpUiState.Error(
                    OSAppError(code = OSAppError.Code.SIGN_UP_FAILURE, cause = result.throwable),
                )
            }
        }
    }

    fun resetState() {
        state = SignUpUiState.Idle
    }

    @HiltViewModel
    class Onboarding @Inject constructor(
        initializeEditBiometricUseCase: InitializeEditBiometricUseCase,
        disableBiometricUseCase: DisableBiometricUseCase,
        getBiometricCipherUseCase: GetBiometricCipherUseCase,
    ) : BiometricCreationViewModel(
        initializeEditBiometricUseCase::invoke,
        disableBiometricUseCase,
        getBiometricCipherUseCase,
    )

    @HiltViewModel
    class ChangePassword @Inject constructor(
        enableBiometricUseCase: EnableBiometricUseCase,
        disableBiometricUseCase: DisableBiometricUseCase,
        getBiometricCipherUseCase: GetBiometricCipherUseCase,
    ) : BiometricCreationViewModel(
        enableBiometricUseCase::invoke,
        disableBiometricUseCase,
        getBiometricCipherUseCase,
    )
}
