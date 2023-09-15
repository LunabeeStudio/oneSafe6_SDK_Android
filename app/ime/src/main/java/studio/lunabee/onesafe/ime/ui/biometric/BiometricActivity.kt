/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 05/09/2023 11:15
 */

package studio.lunabee.onesafe.ime.ui.biometric

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge

@AndroidEntryPoint
class BiometricActivity : FragmentActivity() {

    val viewModel: ImeBiometricViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val biometricPrompt: (() -> Unit) = biometricPrompt(
                labels = DisplayBiometricLabels.Login,
                getCipher = viewModel::getCipher,
                onSuccess = {
                    viewModel.biometricLogin(it)
                    this.finish()
                },
                onFailure = { error ->
                    viewModel.setError(error)
                    this.finish()
                },
                onUserCancel = { this.finish() },
                onNegative = { this.finish() },
            )

            LaunchedEffect(Unit) {
                biometricPrompt.invoke()
            }

            OSTheme {
                ImeBiometricScreen()
            }
        }
    }
}
