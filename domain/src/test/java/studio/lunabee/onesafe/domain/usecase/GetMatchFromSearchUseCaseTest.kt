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

import com.lunabee.lbextensions.lazyFast
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.ClearIndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.test.assertContentEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetMatchFromSearchUseCaseTest {
    val getMatchFromSearchUseCase: GetMatchFromSearchUseCase by lazyFast { GetMatchFromSearchUseCase(itemRepository) }

    @MockK lateinit var itemRepository: SafeItemRepository

    @MockK lateinit var fieldRepository: SafeItemFieldRepository

    private val firstItem: SafeItemWithIdentifier = OSTestUtils.createSafeItemWithIdentifier(id = testUUIDs[0]) // love beer
    private val secondItem: SafeItemWithIdentifier = OSTestUtils.createSafeItemWithIdentifier(id = testUUIDs[1]) // love wine

    private val index: List<ClearIndexWordEntry> = listOf(
        ClearIndexWordEntry("love", testUUIDs[0], null),
        ClearIndexWordEntry("beer", testUUIDs[0], null),
        ClearIndexWordEntry("text", testUUIDs[0], testUUIDs[10]),
        ClearIndexWordEntry("love", testUUIDs[1], null),
        ClearIndexWordEntry("wine", testUUIDs[1], null),
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { itemRepository.getSafeItemWithIdentifier(listOf(testUUIDs[0], testUUIDs[1])) } returns flow {
            emit(
                listOf(
                    firstItem,
                    secondItem,
                ),
            )
        }
        coEvery { itemRepository.getSafeItemWithIdentifier(listOf(testUUIDs[0])) } returns flow { emit(listOf(firstItem)) }
        coEvery { itemRepository.getSafeItemWithIdentifier(listOf(testUUIDs[1])) } returns flow { emit(listOf(secondItem)) }
        coEvery { itemRepository.getSafeItemWithIdentifier(listOf()) } returns flow { emit(listOf()) }
        coEvery { itemRepository.getSafeItemWithIdentifier(listOf(testUUIDs[2])) } returns flow {
            listOf(
                OSTestUtils.createSafeItemWithIdentifier(
                    id = testUUIDs[2],
                ),
            )
        }

        coEvery { fieldRepository.getSafeItemField(testUUIDs[10]) } returns OSTestUtils.createSafeItemField(
            id = testUUIDs[10],
            itemId = testUUIDs[0],
        )
    }

    @Test
    fun no_match_test(): TestResult = runTest {
        val expected = linkedSetOf<SafeItemWithIdentifier>().asIterable()
        val actual = getMatchFromSearchUseCase("aaaa", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun match_one_word_test(): TestResult = runTest {
        val expected = linkedSetOf(firstItem, secondItem).asIterable()
        val actual = getMatchFromSearchUseCase("love", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun match_two_word_test(): TestResult = runTest {
        val expected = linkedSetOf(secondItem).asIterable()
        val actual = getMatchFromSearchUseCase("love wine", index).first()
        assertContentEquals(expected, actual)
    }
}
