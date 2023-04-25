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

package studio.lunabee.onesafe.storage.dao

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.storage.OSStorageTestUtils
import studio.lunabee.onesafe.storage.extension.data
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class SafeItemDaoTest {

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var safeItemDao: SafeItemDao

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun setDeleted_test(): TestResult = runTest {
        val grandParent = OSStorageTestUtils.createRoomSafeItem()
        val parent = OSStorageTestUtils.createRoomSafeItem(parentId = grandParent.id)
        val child = OSStorageTestUtils.createRoomSafeItem(parentId = parent.id)
        val secondChild = OSStorageTestUtils.createRoomSafeItem(parentId = parent.id)
        val grandChild = OSStorageTestUtils.createRoomSafeItem(parentId = child.id)

        safeItemDao.insert(listOf(grandParent, parent, child, secondChild, grandChild))

        val now = Instant.now()
        safeItemDao.setDeletedAndRemoveFromFavorite(parent.id, now)

        val expectedGrandParent = grandParent.copy()
        val expectedParent = parent.copy(deletedAt = now)
        val expectedChild = child.copy(deletedAt = now, deletedParentId = parent.id)
        val expectedSecondChild = secondChild.copy(deletedAt = now, deletedParentId = parent.id)
        val expectedGrandChild = grandChild.copy(deletedAt = now, deletedParentId = child.id)

        val actualGrandParent = safeItemDao.findById(expectedGrandParent.id)
        val actualParent = safeItemDao.findById(parent.id)
        val actualChild = safeItemDao.findById(child.id)
        val actualSecondChild = safeItemDao.findById(secondChild.id)
        val actualGrandChild = safeItemDao.findById(grandChild.id)

        assertEquals(expectedGrandParent, actualGrandParent)
        assertEquals(expectedParent, actualParent)
        assertEquals(expectedChild, actualChild)
        assertEquals(expectedSecondChild, actualSecondChild)
        assertEquals(expectedGrandChild, actualGrandChild)
    }

    @Test
    fun findByIdWithChildren_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[1], parentId = parent.id)
        val grandChild = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[2], parentId = child.id)
        val secondChild = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[3], parentId = parent.id)

        safeItemDao.insert(listOf(parent, child, secondChild, grandChild))

        val expectedItems1 = listOf(child, grandChild)
        val actualItems1 = safeItemDao.findByIdWithDescendants(child.id)
        assertContentEquals(expectedItems1, actualItems1)

        val expectedItems2 = listOf(parent, child, secondChild, grandChild) // child and secondChild order is not mandatory
        val actualItems2 = safeItemDao.findByIdWithDescendants(parent.id)
        assertContentEquals(expectedItems2, actualItems2)
    }

    @Test
    fun findDeletedByParentIdAsPagingSource_delete_child_only_test(): TestResult = runTest {
        val parent1 = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child1 = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent1.id,
            deletedParentId = null,
            deletedAt = Instant.ofEpochMilli(2),
        )

        val parent2 = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[4])
        val child2 = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[5],
            parentId = parent2.id,
            deletedParentId = null,
            deletedAt = Instant.ofEpochMilli(3),
        )

        val parent3 = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[7], deletedAt = Instant.ofEpochMilli(0))
        val child3 = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[8],
            parentId = parent3.id,
            deletedParentId = parent3.id,
            deletedAt = Instant.ofEpochMilli(1),
        )

        safeItemDao.insert(listOf(parent1, child1, parent2, child2, parent3, child3))

        val expectedItems1 = listOf(parent3, child1, child2)
        val actualItems1 = safeItemDao.findByDeletedParentIdAsPagingSource(null).data()
        assertContentEquals(expectedItems1, actualItems1, actualItems1.joinToString())

        val actualItems2 = safeItemDao.findByDeletedParentIdAsPagingSource(child1.id).data()
        assertContentEquals(emptyList(), actualItems2)
    }

    @Test
    fun findDeletedByParentIdAsPagingSource_delete_parent_with_child_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0], deletedAt = Instant.now())
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedParentId = parent.id,
            deletedAt = Instant.now(),
        )

        safeItemDao.insert(listOf(parent, child))

        val expectedRootItems = listOf(parent)
        val actualRootItems = safeItemDao.findByDeletedParentIdAsPagingSource(null).data()
        assertContentEquals(expectedRootItems, actualRootItems)

        val expectedChildItems = listOf(child)
        val actualChildItems = safeItemDao.findByDeletedParentIdAsPagingSource(parent.id).data()
        assertContentEquals(expectedChildItems, actualChildItems)
    }

    @Test
    fun findLastDeletedWithNonDeletedParent_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(3),
        )
        val parent2 = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[4], deletedAt = Instant.ofEpochMilli(2))
        val child2 = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[5],
            parentId = parent2.id,
            deletedParentId = parent2.id,
            deletedAt = Instant.ofEpochMilli(1),
        )

        safeItemDao.insert(listOf(parent, child, parent2, child2))

        val expectedItems = listOf(parent2, child)
        val actualItems = safeItemDao.findLastDeletedWithNonDeletedParent(10).first()
        assertContentEquals(expectedItems, actualItems)
    }

    @Test
    fun countAllDeletedWithNonDeletedParent_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(3),
        )
        val parent2 = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[4], deletedAt = Instant.ofEpochMilli(2))
        val child2 = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[5],
            parentId = parent2.id,
            deletedParentId = parent2.id,
            deletedAt = Instant.ofEpochMilli(1),
        )

        safeItemDao.insert(listOf(parent, child, parent2, child2))

        val expectedCount = 2
        val actualCount = safeItemDao.countAllDeletedWithNonDeletedParent().first()
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun updateParentIdToNonDeletedAncestor_orphan_test(): TestResult = runTest {
        val orphan = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[6], deletedAt = Instant.ofEpochMilli(0))
        safeItemDao.insert(orphan)
        val rowsUpdate = safeItemDao.updateParentIdToFirstNonDeletedAncestor(orphan.id)
        val actual = safeItemDao.findById(orphan.id)!!
        assertNull(actual.parentId)
        assertEquals(0, rowsUpdate)
    }

    @Test
    fun updateParentIdToNonDeletedAncestor_parent_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        safeItemDao.insert(listOf(parent, child))
        val rowsUpdate = safeItemDao.updateParentIdToFirstNonDeletedAncestor(child.id)
        val actual = safeItemDao.findById(child.id)!!
        assertEquals(parent.id, actual.parentId)
        assertEquals(0, rowsUpdate)
    }

    @Test
    fun updateParentIdToNonDeletedAncestor_deleted_parent_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0], deletedAt = Instant.ofEpochMilli(0))
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        safeItemDao.insert(listOf(parent, child))
        val rowsUpdate = safeItemDao.updateParentIdToFirstNonDeletedAncestor(child.id)
        val actual = safeItemDao.findById(child.id)!!
        assertNull(actual.parentId)
        assertEquals(1, rowsUpdate)
    }

    @Test
    fun updateParentIdToNonDeletedAncestor_deleted_parent_deleted_grandparent_test(): TestResult = runTest {
        val grandParent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[9], deletedAt = Instant.ofEpochMilli(0))
        val parent = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[0],
            parentId = grandParent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        safeItemDao.insert(listOf(grandParent, parent, child))
        val rowsUpdate = safeItemDao.updateParentIdToFirstNonDeletedAncestor(child.id)
        val actual = safeItemDao.findById(child.id)!!
        assertNull(actual.parentId)
        assertEquals(1, rowsUpdate)
    }

    @Test
    fun updateParentIdToNonDeletedAncestor_deleted_parent_not_deleted_grandparent_test(): TestResult = runTest {
        val grandParent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[9])
        val parent = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[0],
            parentId = grandParent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        safeItemDao.insert(listOf(grandParent, parent, child))
        val rowsUpdate = safeItemDao.updateParentIdToFirstNonDeletedAncestor(child.id)
        val actual = safeItemDao.findById(child.id)!!
        assertEquals(grandParent.id, actual.parentId)
        assertEquals(1, rowsUpdate)
    }

    @Test
    fun findByIdWithDeletedAncestors_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0], deletedAt = Instant.ofEpochMilli(0))
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedParentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val secondChild = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[3],
            deletedParentId = parent.id,
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val grandChild = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[2],
            parentId = child.id,
            deletedParentId = child.id,
            deletedAt = Instant.ofEpochMilli(0),
        )

        safeItemDao.insert(listOf(parent, child, secondChild, grandChild))

        val expectedItems1 = listOf(parent, child, grandChild)
        val actualItems1 = safeItemDao.findByIdWithDeletedAncestors(grandChild.id)
        assertContentEquals(expectedItems1, actualItems1)

        val expectedItems2 = listOf(parent, secondChild)
        val actualItems2 = safeItemDao.findByIdWithDeletedAncestors(secondChild.id)
        assertContentEquals(expectedItems2, actualItems2)

        val expectedItems3 = listOf(parent)
        val actualItems3 = safeItemDao.findByIdWithDeletedAncestors(parent.id)
        assertContentEquals(expectedItems3, actualItems3)
    }

    @Test
    fun findByIdWithAncestors_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0])
        val child = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[1], parentId = parent.id)
        val secondChild = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[3], parentId = parent.id)
        val grandChild = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[2], parentId = child.id)

        safeItemDao.insert(listOf(parent, child, secondChild, grandChild))

        val expectedItems1 = listOf(parent, child, grandChild)
        val actualItems1 = safeItemDao.findByIdWithAncestors(grandChild.id)
        assertContentEquals(expectedItems1, actualItems1)

        val expectedItems2 = listOf(parent, secondChild)
        val actualItems2 = safeItemDao.findByIdWithAncestors(secondChild.id)
        assertContentEquals(expectedItems2, actualItems2)

        val expectedItems3 = listOf(parent)
        val actualItems3 = safeItemDao.findByIdWithAncestors(parent.id)
        assertContentEquals(expectedItems3, actualItems3)
    }

    @Test
    fun findDeletedByIdWithDeletedDescendants_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[0],
            deletedAt = Instant.ofEpochMilli(0),
        )
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedParentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )

        safeItemDao.insert(listOf(parent, child))

        val expected = listOf(parent, child)
        val actual = safeItemDao.findDeletedByIdWithDeletedDescendants(parent.id)

        assertContentEquals(expected, actual)
    }

    @Test
    fun unsetDeletedAtAndDeletedParentIdForItemAndDescendants_no_descendants_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem()
        val child = OSStorageTestUtils.createRoomSafeItem(
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )

        safeItemDao.insert(listOf(parent, child))

        val expectedChild = child.copy(deletedAt = null)
        safeItemDao.unsetDeletedAtAndDeletedParentIdForItemAndDescendants(child.id)
        assertEquals(expectedChild, safeItemDao.findById(expectedChild.id))
    }

    @Test
    fun unsetDeletedAtAndDeletedParentIdForItemAndDescendants_with_descendants_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem()
        val itemA = OSStorageTestUtils.createRoomSafeItem(
            parentId = parent.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val itemB = OSStorageTestUtils.createRoomSafeItem(
            parentId = itemA.id,
            deletedAt = Instant.ofEpochMilli(0),
            deletedParentId = itemA.id,
        )

        safeItemDao.insert(listOf(parent, itemA, itemB))

        val expectedItemA = itemA.copy(deletedAt = null)
        val expectedItemB = itemB.copy(deletedAt = null, deletedParentId = null)

        safeItemDao.unsetDeletedAtAndDeletedParentIdForItemAndDescendants(itemA.id)

        assertEquals(expectedItemA, safeItemDao.findById(expectedItemA.id))
        assertEquals(expectedItemB, safeItemDao.findById(expectedItemB.id))
    }

    @Test
    fun unsetDeletedAtAndDeletedParentIdForItemAndDescendants_root_test(): TestResult = runTest {
        val itemA = OSStorageTestUtils.createRoomSafeItem(
            deletedAt = Instant.ofEpochMilli(0),
        )
        val itemB = OSStorageTestUtils.createRoomSafeItem(
            parentId = itemA.id,
            deletedParentId = itemA.id,
            deletedAt = Instant.ofEpochMilli(0),
        )
        val nonDeletedItem = OSStorageTestUtils.createRoomSafeItem()
        val itemC = OSStorageTestUtils.createRoomSafeItem(
            parentId = nonDeletedItem.id,
            deletedParentId = null,
            deletedAt = Instant.ofEpochMilli(0),
        )

        safeItemDao.insert(listOf(itemA, itemB, nonDeletedItem, itemC))

        val expectedItemA = itemA.copy(deletedAt = null)
        val expectedItemB = itemB.copy(deletedAt = null, deletedParentId = null)
        val expectedItemC = itemC.copy(deletedAt = null, deletedParentId = null)

        safeItemDao.unsetDeletedAtAndDeletedParentIdForItemAndDescendants(null)

        assertEquals(expectedItemA, safeItemDao.findById(expectedItemA.id))
        assertEquals(expectedItemB, safeItemDao.findById(expectedItemB.id))
        assertEquals(expectedItemC, safeItemDao.findById(expectedItemC.id))
    }
}
