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

package studio.lunabee.onesafe.domain.utils

import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.usecase.search.SearchStringUtils
import kotlin.test.assertContentEquals

class StringUtilsTest {
    @Test
    fun getListStringSearch_test() {
        val tests = listOf(
            "aaa bbb" to listOf("aaa", "bbb"), // nominal case
            "aaa b" to listOf("aaa"), // filter one letter
            "   äÔñQ ,,  " to listOf("aonq"), // trim space and coma + clean string
            "aaa,bbb" to listOf("aaa", "bbb"), // coma as separator
        )
        tests.forEach { (source, expected) ->
            val actual = SearchStringUtils.getListStringSearch(source)
            assertContentEquals(expected = expected, actual = actual, source)
        }
    }
}
