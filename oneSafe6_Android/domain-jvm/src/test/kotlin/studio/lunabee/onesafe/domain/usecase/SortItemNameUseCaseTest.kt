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

import kotlin.test.Test
import studio.lunabee.onesafe.domain.usecase.item.CleanForAlphaIndexingUseCase
import studio.lunabee.onesafe.domain.usecase.item.SortItemNameUseCase
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.test.assertEquals

class SortItemNameUseCaseTest {

    private val useCase: SortItemNameUseCase = SortItemNameUseCase(CleanForAlphaIndexingUseCase())

    private val testCases = mapOf(
        (testUUIDs[0] to "a") to 1.0,
        (testUUIDs[1] to "A") to 1.0,
        (testUUIDs[2] to "b") to 2.0,
        (testUUIDs[3] to "0") to 0.0,
    )

    @Test
    fun onSortedIdName_test() {
        val actualList = useCase(testCases.map { it.key }.shuffled())
        val expectedList = testCases.map { it.key.first to it.value }
        expectedList.forEach { expected ->
            val actual = actualList.first { it.first == expected.first }
            assertEquals(expected, actual)
        }
    }
}
