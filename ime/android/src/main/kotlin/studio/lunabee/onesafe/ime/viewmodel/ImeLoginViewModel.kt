/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 9/5/2023 - for the oneSafe6 SDK.
 * Last modified 05/09/2023 15:09
 */

package studio.lunabee.onesafe.ime.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.domain.usecase.authentication.HasBiometricSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledState
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSImeError
import studio.lunabee.onesafe.ime.model.ImeLoginUiState
import studio.lunabee.onesafe.ime.repository.ImeBiometricResultRepository
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import studio.lunabee.onesafe.login.viewmodel.LoginFromPasswordDelegate
import studio.lunabee.onesafe.login.viewmodel.LoginFromPasswordDelegateImpl
import studio.lunabee.onesafe.login.viewmodel.LoginUiStateHolder

class ImeLoginViewModel(
    hasBiometricSafeUseCase: HasBiometricSafeUseCase,
    private val loginUiStateHolder: LoginUiStateHolder,
    private val loginFromPasswordDelegate: LoginFromPasswordDelegateImpl,
    imeBiometricResultRepository: ImeBiometricResultRepository,
    isSafeReadyUseCase: IsSafeReadyUseCase,
    val versionName: String,
) : ViewModel(loginUiStateHolder, loginFromPasswordDelegate),
    LoginFromPasswordDelegate by loginFromPasswordDelegate {

    private val biometricCipher: Flow<Boolean> = hasBiometricSafeUseCase()
        .map { isBiometricEnabledState ->
            when (isBiometricEnabledState) {
                IsBiometricEnabledState.Disabled -> false
                IsBiometricEnabledState.Enabled -> true
                is IsBiometricEnabledState.Error -> {
                    imeBiometricResultRepository.setError(
                        isBiometricEnabledState.error ?: OSImeError.Code.IME_BIOMETRIC_LOGIN_ERROR.get(),
                    )
                    false
                }
            }
        }.onStart { emit(false) }

    val uiState: StateFlow<ImeLoginUiState> = loginUiStateHolder.uiState
        .combine(biometricCipher) { uiState, biometricCipher ->
            ImeLoginUiState(uiState, biometricCipher)
        }.stateIn(
            scope = viewModelScope,
            started = CommonUiConstants.Flow.DefaultSharingStarted,
            initialValue = ImeLoginUiState(loginUiStateHolder.uiState.value, false),
        )

    private val _biometricError: MutableStateFlow<ErrorSnackbarState?> = MutableStateFlow(null)

    val biometricError: StateFlow<ErrorSnackbarState?> = _biometricError.asStateFlow()

    init {
        // Observe login state
        isSafeReadyUseCase.safeIdFlow()
            .filterNotNull()
            .onEach {
                loginUiStateHolder.dataState
                    ?.copy(loginResult = CommonLoginUiState.LoginResult.Success(false))
                    ?.let { state ->
                        imeBiometricResultRepository.setError(null)
                        loginUiStateHolder.setUiState(state)
                    }
            }
            .launchIn(viewModelScope)

        // Observe error state
        imeBiometricResultRepository.error
            .onEach { error ->
                _biometricError.value = error?.let {
                    ErrorSnackbarState(
                        message = error.description(),
                        onClick = {
                            imeBiometricResultRepository.setError(null)
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
