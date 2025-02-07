package studio.lunabee.onesafe.login.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.qualifier.StoreBetaTrack
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import javax.inject.Inject

@ViewModelScoped
class LoginUiStateHolder @Inject constructor(
    isSafeReadyUseCase: IsSafeReadyUseCase,
    getAppVisitUseCase: GetAppVisitUseCase,
    @StoreBetaTrack isBetaVersion: Boolean,
) : CloseableCoroutineScope by CloseableMainCoroutineScope() {
    private val _uiState: MutableStateFlow<CommonLoginUiState> = MutableStateFlow(CommonLoginUiState.Initialize)

    init {
        coroutineScope.launch {
            // Check crypto on init to set the initial state to set Bypass if needed
            val state = if (isSafeReadyUseCase()) {
                CommonLoginUiState.Bypass
            } else {
                val hasVisitedLogin = getAppVisitUseCase.hasVisitedLogin().first()
                CommonLoginUiState.Data(
                    currentPasswordValue = TextFieldValue(),
                    loginResult = CommonLoginUiState.LoginResult.Idle,
                    isFirstLogin = !hasVisitedLogin,
                    isBeta = isBetaVersion,
                )
            }
            _uiState.value = state
        }
    }

    val uiState: StateFlow<CommonLoginUiState> = _uiState.asStateFlow()
    val dataState: CommonLoginUiState.Data?
        get() = uiState.value as? CommonLoginUiState.Data

    fun setUiState(state: CommonLoginUiState.Data) {
        _uiState.value = state
    }
}
