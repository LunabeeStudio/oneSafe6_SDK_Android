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

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.model.password.GeneratedPassword
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import java.security.SecureRandom
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class GeneratePasswordUseCase @Inject constructor(
    private val estimatePasswordStrengthUseCase: EstimatePasswordStrengthUseCase,
) {
    suspend operator fun invoke(passwordConfig: PasswordConfig): GeneratedPassword {
        val charPool = mutableListOf<Char>()

        if (passwordConfig.includeUpperCase) {
            charPool += ('A'..'Z')
        }
        if (passwordConfig.includeLowerCase) {
            charPool += ('a'..'z')
        }
        if (passwordConfig.includeNumber) {
            charPool += ('0'..'9')
        }
        if (passwordConfig.includeSymbol) {
            val symbols = PasswordConfig.SpecialChars.toCharArray().toList()
            charPool += symbols
        }
        val secureRandom: Random = SecureRandom().asKotlinRandom()
        val builder = StringBuilder(passwordConfig.length)

        var generatedPassword = ""
        while (!passwordConfig.matchesConfig(generatedPassword)) {
            builder.clear()
            repeat(passwordConfig.length) {
                builder.append(charPool.random(secureRandom))
            }
            generatedPassword = builder.toString()
        }
        return GeneratedPassword(
            value = generatedPassword,
            strength = estimatePasswordStrengthUseCase(generatedPassword),
        )
    }
}
