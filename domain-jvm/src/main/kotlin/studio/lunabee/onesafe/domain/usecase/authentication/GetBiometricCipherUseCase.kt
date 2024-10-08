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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.BiometricCipherRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.get
import javax.crypto.Cipher
import javax.inject.Inject

private val log = LBLogger.get<GetBiometricCipherUseCase>()

class GetBiometricCipherUseCase @Inject constructor(
    private val biometricCipherRepository: BiometricCipherRepository,
    private val safeRepository: SafeRepository,
) {
    suspend fun forVerify(): LBResult<Cipher> {
        return OSError.runCatching(log) {
            val key = safeRepository.getBiometricSafe().biometricCryptoMaterial
                ?: throw OSDomainError.Code.MISSING_BIOMETRIC_KEY.get()
            biometricCipherRepository.getCipherBiometricForDecrypt(key.iv)
        }
    }

    fun forCreate(): LBResult<Cipher> {
        return OSError.runCatching(log) {
            biometricCipherRepository.createCipherBiometricForEncrypt()
        }
    }
}
