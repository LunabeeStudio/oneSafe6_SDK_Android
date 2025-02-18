package studio.lunabee.onesafe.feature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.forceupgrade.FetchForceUpgradeDataUseCase
import studio.lunabee.onesafe.domain.usecase.forceupgrade.IsForceUpgradeDisplayedUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    private val isForceUpgradeDisplayedUseCase: IsForceUpgradeDisplayedUseCase,
    private val fetchForceUpgradeDataUseCase: FetchForceUpgradeDataUseCase,
) : ViewModel() {

    var isForceUpgradeDisplayed: Boolean = runBlocking { isForceUpgradeDisplayedUseCase(BuildConfig.VERSION_CODE) }
    var isUserSignUp: Boolean = runBlocking { isSignUpUseCase() }
    private val _sessionState: MutableStateFlow<SessionState> = MutableStateFlow(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        checkSessionState()
        viewModelScope.launch { fetchForceUpgradeDataUseCase() }
    }

    private fun checkSessionState() {
        viewModelScope.launch {
            isSafeReadyUseCase.flow()
                .collect { isSafeReady ->
                    val hasSignIn = savedStateHandle.get<Boolean>(hasSignInKey) ?: false
                    if (!isSafeReady && hasSignIn) {
                        isUserSignUp = isSignUpUseCase()
                        _sessionState.value = SessionState.Broken {
                            _sessionState.value = SessionState.Idle
                        }
                    } else if (isSafeReady) {
                        savedStateHandle[hasSignInKey] = true
                    }
                }
        }
    }

    companion object {
        private const val hasSignInKey = "HAS_SIGN_IN_KEY"
    }
}
