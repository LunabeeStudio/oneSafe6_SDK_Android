package studio.lunabee.onesafe.login.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.SplashScreenManager
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.DisableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.HasBiometricSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledState
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import studio.lunabee.onesafe.login.state.LoginUiState
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    hasBiometricSafeUseCase: HasBiometricSafeUseCase,
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase,
    private val loginUseCase: LoginUseCase,
    @VersionName val versionName: String,
    private val loginUiStateHolder: LoginUiStateHolder,
    private val loginFromPasswordDelegate: LoginFromPasswordDelegateImpl,
    isSafeReadyUseCase: IsSafeReadyUseCase,
    setAppVisitUseCase: SetAppVisitUseCase,
    safeRepository: SafeRepository,
    val splashScreenManager: SplashScreenManager,
) : ViewModel(loginUiStateHolder, loginFromPasswordDelegate),
    LoginFromPasswordDelegate by loginFromPasswordDelegate {

    private val biometricCipher: Flow<(suspend () -> Cipher?)?> = hasBiometricSafeUseCase()
        .map { isBiometricEnabledState ->
            when (isBiometricEnabledState) {
                IsBiometricEnabledState.Disabled -> null
                IsBiometricEnabledState.Enabled -> ::getCipher
                is IsBiometricEnabledState.Error -> {
                    _snackbarState.emit(ErrorSnackbarState(error = isBiometricEnabledState.error, onClick = {}))
                    null
                }
            }
        }.onStart { emit(null) }

    val uiState: StateFlow<LoginUiState> = loginUiStateHolder.uiState
        .combine(biometricCipher) { uiState, biometricCipher ->
            LoginUiState(uiState, biometricCipher)
        }.stateIn(
            scope = viewModelScope,
            started = CommonUiConstants.Flow.DefaultSharingStarted,
            initialValue = LoginUiState(loginUiStateHolder.uiState.value, null),
        )

    private val _snackbarState = MutableSharedFlow<SnackbarState?>()

    val snackbarState: SharedFlow<SnackbarState?> = _snackbarState.asSharedFlow()

    private val lastSafeId: SafeId? = safeRepository.lastSafeIdLoaded()

    init {
        // Observe the crypto to set the Success state (in case of crypto loaded by another activity)
        // See https://www.notion.so/lunabeestudio/Cannot-unlock-oneSafe-from-file-picker-if-already-unlock-in-app-40d8b46a7fb54d1c90b832d4a4d2664c
        isSafeReadyUseCase.safeIdFlow()
            .filterNotNull()
            .onEach { safeId ->
                loginUiStateHolder.dataState
                    ?.copy(loginResult = CommonLoginUiState.LoginResult.Success(lastSafeId == safeId))
                    ?.let { state -> loginUiStateHolder.setUiState(state) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            setAppVisitUseCase.setHasVisitedLoginKey()
        }
    }

    private suspend fun getCipher(): Cipher? {
        val result = getBiometricCipherUseCase.forVerify()
        if (result is LBResult.Failure) {
            disableBiometricUseCase()
            _snackbarState.emit(ErrorSnackbarState(error = result.throwable, onClick = {}))
        }
        return result.data
    }

    fun biometricLogin(cipher: Cipher) {
        viewModelScope.launch {
            val loginResult = loginUseCase(cipher)
            when (loginResult) {
                is LBResult.Success -> {
                    /* no-op, observe safeReadyUseCase */
                }
                is LBResult.Failure -> {
                    loginUiStateHolder.dataState?.copy(
                        currentPasswordValue = TextFieldValue(),
                        loginResult = CommonLoginUiState.LoginResult.Error,
                    )?.let { dataState ->
                        loginUiStateHolder.setUiState(dataState)
                    }
                }
            }
        }
    }
}
