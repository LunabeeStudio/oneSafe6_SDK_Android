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
 * Last modified 05/09/2023 09:34
 */

package studio.lunabee.onesafe.ime.ui

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.ime.R
import studio.lunabee.onesafe.ime.ui.biometric.BiometricActivity
import studio.lunabee.onesafe.ime.viewmodel.ImeLoginViewModel
import studio.lunabee.onesafe.login.screen.LoginExitIcon
import studio.lunabee.onesafe.login.screen.LoginScreenWrapper
import studio.lunabee.onesafe.login.state.LoginUiState

@Composable
fun ImeLoginRoute(
    onSuccess: () -> Unit,
    onClose: () -> Unit,
    viewModel: ImeLoginViewModel,
) {
    val loginUiState: LoginUiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val uiState = loginUiState) {
        LoginUiState.Initialize -> {
            /* no-op */
        }
        LoginUiState.Bypass -> onSuccess()
        is LoginUiState.Data -> {
            val isSuccess = uiState.loginResult is LoginUiState.LoginResult.Success
            LaunchedEffect(isSuccess) {
                if (isSuccess) {
                    onSuccess()
                }
            }

            val exitIcon: LoginExitIcon = LoginExitIcon.Close(onClose)
            val context = LocalContext.current
            val onBiometricClick: () -> Unit = {
                val intent = Intent(context, BiometricActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
                context.startActivity(intent)
            }

            val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
            val errorSnackbarState: ErrorSnackbarState? by viewModel.biometricError.collectAsStateWithLifecycle()
            errorSnackbarState?.LaunchedSnackbarEffect(snackbarHostState)

            LoginScreenWrapper(
                exitIcon = exitIcon,
                uiState = uiState,
                setPasswordValue = viewModel::setPasswordValue,
                versionName = viewModel.versionName,
                loginFromPassword = viewModel::loginFromPassword,
                onBiometricClick = onBiometricClick,
                logoRes = R.drawable.onesafek_logo,
                snackbarHostState = snackbarHostState,
                isIllustrationDisplayed = false,
            )
        }
    }
}
