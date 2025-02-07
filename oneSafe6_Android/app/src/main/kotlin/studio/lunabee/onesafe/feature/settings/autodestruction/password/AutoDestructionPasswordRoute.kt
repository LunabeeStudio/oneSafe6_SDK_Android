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
 * Last modified 12/09/2024 08:49
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.feature.password.creation.PasswordCreationScreen
import studio.lunabee.onesafe.feature.password.creation.PasswordCreationScreenLabels

@Composable
fun AutoDestructionPasswordRoute(
    navScope: AutoDestructionPasswordNavScope,
    viewModel: AutoDestructionPasswordViewModel = hiltViewModel(),
) {
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember { derivedStateOf { passwordValue.isNotBlank() } }
    val uiState: AutoDestructionPasswordCreationUiState by viewModel.uiState.collectAsStateWithLifecycle()

    (uiState as? AutoDestructionPasswordCreationUiState.Success)?.let { state ->
        LaunchedEffect(Unit) {
            navScope.navigateToConfirm(state.passwordHash, state.salt)
            viewModel.resetUiState(state.passwordHash)
        }
    }

    PasswordCreationScreen(
        labels = PasswordCreationScreenLabels.AutoDestruction,
        navigateBack = navScope.navigateBack,
        confirmClick = {
            viewModel.verifyPassword(
                password = passwordValue,
            )
        },
        isConfirmEnabled = isConfirmEnabled && uiState is AutoDestructionPasswordCreationUiState.Idle,
        passwordValue = passwordValue,
        passwordLevelText = (uiState as? AutoDestructionPasswordCreationUiState.Idle)?.passwordStrength,
        onValueChange = {
            passwordValue = it
            viewModel.resetUiState(passwordValue)
        },
        isLoading = false,
        error = (uiState as? AutoDestructionPasswordCreationUiState.Error)?.text,
    )
}
