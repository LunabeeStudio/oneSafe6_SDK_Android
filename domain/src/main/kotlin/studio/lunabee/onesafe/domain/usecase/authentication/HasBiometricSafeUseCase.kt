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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.osCode
import javax.inject.Inject

// TODO unit test
/**
 * Check if biometric is enabled for at least one Safe. In case of unrecoverable error, reset the data and emit failure.
 */
class HasBiometricSafeUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val disableBiometricUseCase: DisableBiometricUseCase,
) {
    operator fun invoke(): Flow<IsBiometricEnabledState> = flow {
        val hasBiometricResult = OSError.runCatching {
            val isBiometricEnabledStateFlow = safeRepository.hasBiometricSafe().map { isEnabled ->
                if (isEnabled) IsBiometricEnabledState.Enabled else IsBiometricEnabledState.Disabled
            }
            emitAll(isBiometricEnabledStateFlow)
        }
        if (hasBiometricResult is LBResult.Failure) {
            if (hasBiometricResult.throwable.osCode() == OSCryptoError.Code.ANDROID_KEYSTORE_KEY_PERMANENTLY_INVALIDATE) {
                disableBiometricUseCase()
            }
            emit(IsBiometricEnabledState.Error(hasBiometricResult.throwable))
        }
    }
}

sealed class IsBiometricEnabledState(val isEnabled: Boolean) {
    data object Enabled : IsBiometricEnabledState(true)
    data object Disabled : IsBiometricEnabledState(false)
    data class Error(val error: Throwable?) : IsBiometricEnabledState(false)
}
