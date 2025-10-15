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

import studio.lunabee.onesafe.domain.common.SortedAncestors
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SortedAncestorsTest {
    private val safeItemsNotSorted: MutableList<SafeItem> = mutableListOf<SafeItem>().apply {
        // 0
        // (1) -> (4, 5 -> (6, 7))
        // 2
        // 3
        // (8) -> (9, 10 -> (11 -> (13), 12))
        this += OSTestUtils.createSafeItem(id = testUUIDs[0], parentId = null)
        this += OSTestUtils.createSafeItem(id = testUUIDs[1], parentId = null)
        this += OSTestUtils.createSafeItem(id = testUUIDs[2], parentId = null)
        this += OSTestUtils.createSafeItem(id = testUUIDs[3], parentId = null)
        this += OSTestUtils.createSafeItem(id = testUUIDs[4], parentId = testUUIDs[1])
        this += OSTestUtils.createSafeItem(id = testUUIDs[5], parentId = testUUIDs[1])
        this += OSTestUtils.createSafeItem(id = testUUIDs[6], parentId = testUUIDs[5])
        this += OSTestUtils.createSafeItem(id = testUUIDs[7], parentId = testUUIDs[5])
        this += OSTestUtils.createSafeItem(id = testUUIDs[8], parentId = null)
        this += OSTestUtils.createSafeItem(id = testUUIDs[9], parentId = testUUIDs[8])
        this += OSTestUtils.createSafeItem(id = testUUIDs[10], parentId = testUUIDs[8])
        this += OSTestUtils.createSafeItem(id = testUUIDs[11], parentId = testUUIDs[10])
        this += OSTestUtils.createSafeItem(id = testUUIDs[12], parentId = testUUIDs[10])
        this += OSTestUtils.createSafeItem(id = testUUIDs[13], parentId = testUUIDs[11])
    }

    @Test
    fun sort_ancestor_test() {
        // Repeat 100 times to test random sort
        repeat((0..100).count()) {
            println("\n\nTest number $it")
            val seed = Random.nextInt()
            val shuffledItem = safeItemsNotSorted.shuffled(Random(seed))
            // Display initial list content if test fail (but it should not happen)
            println("Not sorted list content with seed $seed")
            shuffledItem.forEach { item -> println("id = ${item.id}, parentId = ${item.parentId}") }

            val sortedAncestors = SortedAncestors(safeItemsNotSorted = shuffledItem)
            val result = sortedAncestors.sortByAncestors()
            println("\n\nSorted list content")
            result.forEachIndexed { index, item ->
                println("id = ${item.id}, parentId = ${item.parentId}")
                if (item.parentId != null) {
                    assert(index > result.indexOfFirst { it.id == item.parentId })
                }
            }
        }
    }

    @Test
    fun sort_ancestor_with_recursive_item_test() {
        val recursiveItem = OSTestUtils.createSafeItem(id = testUUIDs[0], parentId = testUUIDs[0])
        val sortedAncestors = SortedAncestors(safeItemsNotSorted = listOf(recursiveItem))

        val expected = listOf(recursiveItem.copy(parentId = null))
        val actual = sortedAncestors.sortByAncestors()
        assertContentEquals(expected, actual)
    }
}
