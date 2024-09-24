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
 * Last modified 05/09/2023 11:40
 */

package studio.lunabee.onesafe.ime.ui.biometric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSImeError
import studio.lunabee.onesafe.ime.repository.ImeBiometricResultRepository
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class ImeBiometricViewModel @Inject constructor(
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
    private val imeBiometricResultRepository: ImeBiometricResultRepository,
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    suspend fun getCipher(): Cipher? {
        return getBiometricCipherUseCase.forVerify().data
    }

    fun biometricLogin(cipher: Cipher) {
        viewModelScope.launch {
            val result: LBResult<Unit> = loginUseCase(cipher)
            when (result) {
                is LBResult.Failure -> {
                    val error = result.throwable ?: OSImeError.Code.IME_BIOMETRIC_LOGIN_ERROR.get()
                    imeBiometricResultRepository.setError(error)
                }
                is LBResult.Success -> {
                    /* no-op, observe safeReadyUseCase */
                }
            }
        }
    }

    fun setError(error: OSAppError) {
        imeBiometricResultRepository.setError(error)
    }
}
