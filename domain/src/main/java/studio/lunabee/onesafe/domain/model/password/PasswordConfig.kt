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

package studio.lunabee.onesafe.domain.model.password

data class PasswordConfig(
    val length: Int,
    val includeUpperCase: Boolean,
    val includeLowerCase: Boolean,
    val includeNumber: Boolean,
    val includeSymbol: Boolean,
) {
    init {
        check(length in MinLength..MaxLength)
    }

    companion object {
        const val MinLength: Int = 8
        const val DefaultLength: Int = 18
        const val MaxLength: Int = 30

        fun default(): PasswordConfig = PasswordConfig(
            length = DefaultLength,
            includeLowerCase = true,
            includeUpperCase = true,
            includeNumber = true,
            includeSymbol = true,
        )

        const val NumberPattern: String = ".*[1-9]+.*"
        const val LowerCasePattern: String = ".*[a-z]+.*"
        const val UpperCasePattern: String = ".*[A-Z]+.*"
        const val SpecialChars: String = ".+^\$*\\[\\]{}()?\"!@#%&/\\,><':;|_-`=€£¥~"
        const val SpecialCharPattern: String = ".*[$SpecialChars]+.*"
    }

    fun matchesConfig(password: String): Boolean =
        (password.length == length)
            && ((includeUpperCase && password.matches(Regex(UpperCasePattern))) || !includeUpperCase)
            && ((includeLowerCase && password.matches(Regex(LowerCasePattern))) || !includeLowerCase)
            && ((includeNumber && password.matches(Regex(NumberPattern))) || !includeNumber)
            && ((includeSymbol && password.matches(Regex(SpecialCharPattern))) || !includeSymbol)
}
