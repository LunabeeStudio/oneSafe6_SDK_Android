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
 * Created by Lunabee Studio / Date - 5/15/2024 - for the oneSafe6 SDK.
 * Last modified 5/15/24, 12:57 PM
 */

package studio.lunabee.onesafe.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.gosimple.nbvcxz.Nbvcxz
import studio.lunabee.onesafe.domain.model.password.PasswordStrength
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import javax.inject.Inject

class EstimatePasswordStrengthUseCase @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val strengthEstimator = Nbvcxz()

    suspend operator fun invoke(password: String): PasswordStrength {
        val entropy = withContext(dispatcher) {
            strengthEstimator.estimate(password).entropy
        }
        return when {
            entropy < 25 -> PasswordStrength.VeryWeak
            entropy >= 25 && entropy < 50 -> PasswordStrength.Weak
            entropy >= 50 && entropy < 75 -> PasswordStrength.Good
            entropy >= 75 && entropy < 85 -> PasswordStrength.Strong
            entropy >= 85 && entropy < 100 -> PasswordStrength.VeryStrong
            entropy >= 100 -> PasswordStrength.BulletProof
            else -> PasswordStrength.Unknown
        }
    }
}
