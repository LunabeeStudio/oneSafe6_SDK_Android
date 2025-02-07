/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 08:53
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.extensions.label
import studio.lunabee.onesafe.domain.usecase.EstimatePasswordStrengthUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.DeriveAutoDestructionPasswordUseCase
import studio.lunabee.onesafe.domain.utils.SaltProvider
import javax.inject.Inject

@HiltViewModel
class AutoDestructionPasswordViewModel @Inject constructor(
    private val estimatePasswordStrengthUseCase: EstimatePasswordStrengthUseCase,
    private val isPasswordCorrectUseCase: IsPasswordCorrectUseCase,
    private val saltProvider: SaltProvider,
    private val deriveAutoDestructionPasswordUseCase: DeriveAutoDestructionPasswordUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<AutoDestructionPasswordCreationUiState> = MutableStateFlow(
        AutoDestructionPasswordCreationUiState.Idle(passwordStrength = null),
    )
    val uiState: StateFlow<AutoDestructionPasswordCreationUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalStdlibApi::class)
    fun verifyPassword(password: String) {
        viewModelScope.launch {
            val isPasswordCorrectResult = isPasswordCorrectUseCase(password.toCharArray())
            if (isPasswordCorrectResult is LBResult.Success) { // password is the same as before
                _uiState.value = AutoDestructionPasswordCreationUiState.Error
            } else {
                val salt = saltProvider().toHexString()
                val passwordHash = deriveAutoDestructionPasswordUseCase(password, salt)
                _uiState.value = AutoDestructionPasswordCreationUiState.Success(passwordHash, salt)
            }
        }
    }

    fun resetUiState(password: String) {
        viewModelScope.launch {
            _uiState.value = AutoDestructionPasswordCreationUiState.Idle(getPasswordStrength(password))
        }
    }

    private suspend fun getPasswordStrength(password: String): LbcTextSpec? {
        return password.takeUnless { it.isEmpty() }?.let { estimatePasswordStrengthUseCase(password).label() }
    }
}
