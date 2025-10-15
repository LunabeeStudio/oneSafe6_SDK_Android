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
 * Created by Lunabee Studio / Date - 10/24/2023 - for the oneSafe6 SDK.
 * Last modified 10/24/23, 11:50 AM
 */

package studio.lunabee.onesafe.domain.utils

import studio.lunabee.onesafe.domain.utils.FileHelper.clearExtension
import studio.lunabee.onesafe.domain.utils.FileHelper.getValidFileName
import kotlin.test.Test
import kotlin.test.assertEquals

class FileHelperTest {
    @Test
    fun getValidFileName_test() {
        val extension = "jpg"
        val testCases = listOf(
            "test.$extension" to "test.$extension",
            "test/test" to "test_test.$extension",
            "test*" to "test_.$extension",
            "<test>" to "_test_.$extension",
            "?test" to "_test.$extension",
            "test|test" to "test_test.$extension",
            "test'" to "test_.$extension",
            "test $extension" to "test $extension.$extension",
        )
        testCases.forEachIndexed { idx, testCase ->
            assertEquals(testCase.second, testCase.first.getValidFileName(extension), "Fail at $idx")
        }
    }

    @Test
    fun clearExtension_test() {
        val extension = "jpg"
        val testCases = listOf(
            "test.$extension" to "test",
            ".$extension" to ".$extension",
            " .$extension" to " .$extension",
        )
        testCases.forEachIndexed { idx, testCase ->
            assertEquals(testCase.second, testCase.first.clearExtension(), "Fail at $idx")
        }
    }
}
