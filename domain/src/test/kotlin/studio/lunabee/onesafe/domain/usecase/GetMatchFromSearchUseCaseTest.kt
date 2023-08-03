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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.search.GetMatchFromSearchUseCase
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import java.util.UUID
import kotlin.test.assertContentEquals

class GetMatchFromSearchUseCaseTest {
    val getMatchFromSearchUseCase: GetMatchFromSearchUseCase by lazyFast { GetMatchFromSearchUseCase(itemRepository) }

    private val itemMap: Map<UUID, SafeItemWithIdentifier> = List(20) { idx ->
        testUUIDs[idx] to OSTestUtils.createSafeItemWithIdentifier(
            id = testUUIDs[idx],
            position = (idx % 10).toDouble(),
            updatedAt = Instant.ofEpochMilli((idx % 5).toLong()),
        )
    }.toMap()
    private val items = itemMap.values.toList()

    private val fields = mapOf(
        testUUIDs[10] to OSTestUtils.createSafeItemField(
            id = testUUIDs[100],
            itemId = testUUIDs[0],
        ),
    )

    @MockK lateinit var itemRepository: SafeItemRepository

    @MockK lateinit var fieldRepository: SafeItemFieldRepository

    private val index: List<PlainIndexWordEntry> = listOf(
        PlainIndexWordEntry("aaaa", testUUIDs[0], null),
        PlainIndexWordEntry("bbbb", testUUIDs[0], null),
        PlainIndexWordEntry("field aabb", testUUIDs[0], testUUIDs[10]),

        PlainIndexWordEntry("aaaa", testUUIDs[1], testUUIDs[11]),
        PlainIndexWordEntry("cccc", testUUIDs[1], null),
        PlainIndexWordEntry("dddd", testUUIDs[1], null),

        PlainIndexWordEntry("zzzz", testUUIDs[2], null),
        PlainIndexWordEntry("yyyy", testUUIDs[2], null),
        PlainIndexWordEntry("xxxx", testUUIDs[2], null),

        PlainIndexWordEntry("zzzz", testUUIDs[3], null),
        PlainIndexWordEntry("yyyy", testUUIDs[3], null),

        PlainIndexWordEntry("zzzz", testUUIDs[4], null),
        PlainIndexWordEntry("zzzz", testUUIDs[4], null),
        PlainIndexWordEntry("zzzz", testUUIDs[4], null),
        PlainIndexWordEntry("yyyy", testUUIDs[4], null),
        PlainIndexWordEntry("yyyy", testUUIDs[4], null),
        PlainIndexWordEntry("yyyy", testUUIDs[4], null),

        PlainIndexWordEntry("zzzz", testUUIDs[5], null),
        PlainIndexWordEntry("yyyy", testUUIDs[5], testUUIDs[11]),

        PlainIndexWordEntry("zzzz", testUUIDs[8], null),
        PlainIndexWordEntry("yyyy", testUUIDs[8], null),

        PlainIndexWordEntry("zzzz", testUUIDs[9], null),
        PlainIndexWordEntry("yyyy", testUUIDs[9], null),
        PlainIndexWordEntry("xxxx", testUUIDs[9], null),
        PlainIndexWordEntry("9999", testUUIDs[9], null),
    ).shuffled(OSTestUtils.random)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { itemRepository.getSafeItemWithIdentifier(any()) } answers {
            flowOf(firstArg<List<UUID>>().mapNotNull { itemMap[it] })
        }

        coEvery { fieldRepository.getSafeItemField(any()) } answers { fields[firstArg()]!! }
    }

    @Test
    fun no_match_test(): TestResult = runTest {
        val expected = linkedSetOf<SafeItemWithIdentifier>().asIterable()
        val actual = getMatchFromSearchUseCase("1234", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun match_one_word_test(): TestResult = runTest {
        // order 0,1 because item match > field match
        val expected = linkedSetOf(items[0], items[1]).asIterable()
        val actual = getMatchFromSearchUseCase("aaaa", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun match_two_word_test(): TestResult = runTest {
        val expected = linkedSetOf(items[1]).asIterable()
        val actual = getMatchFromSearchUseCase("cccc dddd", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun partial_word_match(): TestResult = runTest {
        val expected = linkedSetOf(items[0]).asIterable()
        val actual = getMatchFromSearchUseCase("fiel", index).first()
        assertContentEquals(expected, actual)
    }

    @Test
    fun match_score_test(): TestResult = runTest {
        val expected = linkedSetOf(
            items[2], // match 3 keywords (3/3 in title)
            items[9], // match 3 keywords (2/4 in title)
            items[4], // match 2 keywords + 6 total matches
            items[3], // match 2 keywords + 2 total matches + updatedAt 3 + position 3
            items[8], // match 2 keywords + 2 total matches + updatedAt 3 + position 8
            items[5], // match 2 keywords + 2 total matches + updatedAt 0
        ).asIterable()
        val actual = getMatchFromSearchUseCase("yyyy xxxx zzzz", index).first()
        assertContentEquals(expected, actual)
    }
}
