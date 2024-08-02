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
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.GetBiometricCipherUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSError.Companion.get
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class ImeBiometricViewModel @Inject constructor(
    private val getBiometricCipherUseCase: GetBiometricCipherUseCase,
    private val imeBiometricResultRepository: ImeBiometricResultRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) : ViewModel() {

    fun getCipher(): Cipher? {
        return runBlocking { getBiometricCipherUseCase.forVerify().data } // TODO <multisafe> clean runBlocking + handle result
    }

    fun biometricLogin(cipher: Cipher) {
        viewModelScope.launch {
            // TODO <multisafe> move to usecase
            val result = OSError.runCatching {
                safeRepository.getBiometricSafe().biometricCryptoMaterial?.let { encKey ->
                    cryptoRepository.decryptMasterKeyWithBiometric(encKey, cipher)
                } ?: throw OSAppError.Code.BIOMETRIC_LOGIN_ERROR.get("Unexpected null encBiometricMasterKey")
            }
            imeBiometricResultRepository.setResult(result)
        }
    }

    fun setError(error: OSAppError) {
        viewModelScope.launch {
            imeBiometricResultRepository.setResult(LBResult.Failure(error))
        }
    }
}
