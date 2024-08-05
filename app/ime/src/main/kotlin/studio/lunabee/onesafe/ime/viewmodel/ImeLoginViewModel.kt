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
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.HasBiometricSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledState
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.ime.ui.biometric.ImeBiometricResultRepository
import studio.lunabee.onesafe.login.state.LoginUiState
import studio.lunabee.onesafe.login.viewmodel.LoginFromPasswordDelegate
import studio.lunabee.onesafe.login.viewmodel.LoginFromPasswordDelegateImpl
import studio.lunabee.onesafe.login.viewmodel.LoginUiStateHolder

class ImeLoginViewModel(
    hasBiometricSafeUseCase: HasBiometricSafeUseCase,
    private val loginUiStateHolder: LoginUiStateHolder,
    private val loginFromPasswordDelegate: LoginFromPasswordDelegateImpl,
    private val imeBiometricResultRepository: ImeBiometricResultRepository,
    private val mainCryptoRepository: MainCryptoRepository,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    val versionName: String,
) : ViewModel(),
    LoginFromPasswordDelegate by loginFromPasswordDelegate {
    val uiState: StateFlow<LoginUiState> = loginUiStateHolder.uiState

    val isBiometricEnabled: StateFlow<Boolean> = hasBiometricSafeUseCase().map { result ->
        if (result is IsBiometricEnabledState.Error) {
            _biometricError.emit(ErrorSnackbarState(error = result.error, onClick = {}))
        }
        result.isEnabled
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        false,
    )

    private val _biometricError: MutableStateFlow<ErrorSnackbarState?> = MutableStateFlow(null)
    val biometricError: StateFlow<ErrorSnackbarState?> = _biometricError.asStateFlow()

    init {
        isSafeReadyUseCase.safeIdFlow()
            .filterNotNull()
            .onEach { safeId ->
                loginUiStateHolder.dataState
                    ?.copy(loginResult = LoginUiState.LoginResult.Success(false))
                    ?.let { state -> loginUiStateHolder.setUiState(state) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            imeBiometricResultRepository.result.transformWhile {
                emit(it)
                it is LBResult.Failure
            }.collect { result ->
                when (result) {
                    is LBResult.Failure -> {
                        _biometricError.value = ErrorSnackbarState(
                            (result.throwable as? OSError).description(),
                        ) {}
                    }
                    is LBResult.Success -> {
                        mainCryptoRepository.loadMasterKeyExternal(result.successData)
                        loginUiStateHolder.dataState?.copy(
                            loginResult = LoginUiState.LoginResult.Success(false),
                        )?.let { dataState ->
                            loginUiStateHolder.setUiState(dataState)
                        }
                    }
                }
            }
        }
    }
}
