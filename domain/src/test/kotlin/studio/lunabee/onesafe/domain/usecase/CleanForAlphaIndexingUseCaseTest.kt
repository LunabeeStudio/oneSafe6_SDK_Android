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
 * Created by Lunabee Studio / Date - 12/11/2023 - for the oneSafe6 SDK.
 * Last modified 12/8/23, 5:57 PM
 */

package studio.lunabee.onesafe.domain.usecase

import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.usecase.item.CleanForAlphaIndexingUseCase
import kotlin.test.assertEquals

class CleanForAlphaIndexingUseCaseTest {

    private val useCase: CleanForAlphaIndexingUseCase = CleanForAlphaIndexingUseCase()

    @Test
    fun cleanForIndexing_test() {
        val testCases = mapOf(
            "🥹〄a🪂 b😶‍🌫️" to "a b", // remove emoji + trim
            "123abcABC️" to "123abcabc", // keep num & alpha + lowercase
            "@# &()!" to "@# &()!", // keep punctuation
            "éàçäйά" to "eacaиα", // remove diacritic
            "ライカ райка ράικα" to "ライカ раика ραικα", // keep non latin char
            "🦊 123 ?.🐾:õ雪abc" to "123 ?.:o雪abc", // mix
        )

        testCases.forEach { (testText, expected) ->
            val actual = useCase(testText)
            assertEquals(expected, actual)
        }
    }
}
