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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.storage.OSStorageTestUtils
import studio.lunabee.onesafe.storage.extension.data
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@HiltAndroidTest
class SafeItemRawDaoTest {

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var safeItemDao: SafeItemDao

    @Inject internal lateinit var safeItemFieldDao: SafeItemFieldDao

    @Inject internal lateinit var rawDao: SafeItemRawDao

    private lateinit var itemA: RoomSafeItem
    private lateinit var itemB: RoomSafeItem
    private lateinit var itemA1: RoomSafeItem

    private lateinit var fieldA: SafeItemField
    private lateinit var fieldB: SafeItemField
    private lateinit var fieldA1: SafeItemField

    @Before
    fun setUp() {
        hiltRule.inject()

        itemA = OSStorageTestUtils.createRoomSafeItem(
            position = 0.0,
            indexAlpha = 3.0,
            updatedAt = Instant.ofEpochMilli(2),
        )
        itemB = OSStorageTestUtils.createRoomSafeItem(
            position = 1.0,
            indexAlpha = 1.0,
            isFavorite = true,
            updatedAt = Instant.ofEpochMilli(0),
        )
        itemA1 = OSStorageTestUtils.createRoomSafeItem(
            parentId = itemA.id,
            position = 0.0,
            indexAlpha = 2.0,
            isFavorite = true,
            updatedAt = Instant.ofEpochMilli(3),
        )

        fieldA = OSTestUtils.createSafeItemField(itemId = itemA.id, isItemIdentifier = true)
        fieldB = OSTestUtils.createSafeItemField(itemId = itemB.id, isItemIdentifier = true)
        fieldA1 = OSTestUtils.createSafeItemField(itemId = itemA1.id, isItemIdentifier = true)

        runTest {
            safeItemDao.insert(listOf(itemA, itemB, itemA1))
            safeItemFieldDao.insert(listOf(fieldA, fieldB, fieldA1).map(RoomSafeItemField::fromSafeItemField))
        }
    }

    @Test
    fun findByParentId_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expectedNull = listOf(itemA, itemB).sortedBy(itemOrder)
            val actualNull = rawDao.getItems(SafeItemRawDao.findByParentIdQuery(null, itemOrder))

            assertContentEquals(expectedNull, actualNull, itemOrder)
            val expectedA = listOf(itemA1)
            val actualA = rawDao.getItems(SafeItemRawDao.findByParentIdQuery(itemA.id, itemOrder))

            assertContentEquals(expectedA, actualA, itemOrder)
            val actualB = rawDao.getItems(SafeItemRawDao.findByParentIdQuery(itemB.id, itemOrder))

            assertContentEquals(emptyList(), actualB, itemOrder)
        }
    }

    @Test
    fun findDeletedByParentIdNotEqualDeletedParentId_test(): TestResult = runTest {
        val itemDeleted = OSStorageTestUtils.createRoomSafeItem(
            parentId = itemA.id,
            deletedParentId = itemA1.id,
            position = 0.0,
            indexAlpha = 2.0,
            updatedAt = Instant.ofEpochMilli(3),
            deletedAt = Instant.ofEpochMilli(10),
        )

        safeItemDao.insert(itemDeleted)

        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemDeleted)
            val actual = rawDao.getItems(
                SafeItemRawDao.findDeletedByParentIdNotEqualDeletedParentIdQuery(itemA.id, itemOrder),
            )

            assertContentEquals(expected, actual, itemOrder)
        }
    }

    @Test
    fun findByParentIdAsPagingSource_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expectedNull = listOf(itemA, itemB).sortedBy(itemOrder)
            val actualNull = rawDao.getPagedItems(SafeItemRawDao.findByParentIdQuery(null, itemOrder))
                .data()
            assertContentEquals(expectedNull, actualNull, itemOrder)
            val expectedA = listOf(itemA1)
            val actualA = rawDao.getPagedItems(SafeItemRawDao.findByParentIdQuery(itemA.id, itemOrder))
                .data()
            assertContentEquals(expectedA, actualA, itemOrder)
            val actualB = rawDao.getPagedItems(SafeItemRawDao.findByParentIdQuery(itemB.id, itemOrder))
                .data()
            assertContentEquals(emptyList(), actualB, itemOrder)
        }
    }

    @Test
    fun getAllSafeItemsWithIdentifierAsPagingSource_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA, itemA1).sortedBy(itemOrder)
            val actual = rawDao.getPagedSafeItemsWithIdentifier(
                SafeItemRawDao.getAllSafeItemsWithIdentifierQuery(
                    idsToExclude = listOf(itemB.id),
                    order = itemOrder,
                ),
            ).data()

            assertContentEqualsIdentifier(expected, actual, itemOrder)
        }
    }

    @Test
    fun findByDeletedParentIdAsPagingSource_delete_child_only_test(): TestResult = runTest {
        safeItemDao.clearTable()

        val parent1 = OSStorageTestUtils.createRoomSafeItem(position = 0.0, id = testUUIDs[0])
        val child1 = OSStorageTestUtils.createRoomSafeItem(
            position = 1.0,
            id = testUUIDs[1],
            parentId = parent1.id,
            deletedAt = Instant.ofEpochMilli(5),
        )

        val parent2 = OSStorageTestUtils.createRoomSafeItem(position = 2.0, id = testUUIDs[4])
        val child2 = OSStorageTestUtils.createRoomSafeItem(
            position = 3.0,
            id = testUUIDs[5],
            parentId = parent2.id,
            deletedAt = Instant.ofEpochMilli(4),
        )

        val parent3 = OSStorageTestUtils.createRoomSafeItem(
            position = 4.0,
            id = testUUIDs[7],
            deletedAt = Instant.ofEpochMilli(1),
        )
        val child3 = OSStorageTestUtils.createRoomSafeItem(
            position = 5.0,
            id = testUUIDs[8],
            parentId = parent3.id,
            deletedParentId = parent3.id,
            deletedAt = Instant.ofEpochMilli(2),
        )

        safeItemDao.insert(listOf(parent1, child1, parent2, child2, parent3, child3))

        ItemOrder.entries.forEach { itemOrder ->
            val expectedItems1 = listOf(parent3, child1, child2)
            val actualItems1 = rawDao
                .getPagedItems(SafeItemRawDao.findByDeletedParentIdQuery(null, itemOrder))
                .data()
            assertContentEquals(expectedItems1, actualItems1, itemOrder)

            val actualItems2 = rawDao
                .getPagedItems(SafeItemRawDao.findByDeletedParentIdQuery(child1.id, itemOrder))
                .data()
            assertContentEquals(emptyList(), actualItems2, itemOrder)
        }
    }

    @Test
    fun findDeletedByParentIdAsPagingSource_delete_parent_with_child_test(): TestResult = runTest {
        val parent = OSStorageTestUtils.createRoomSafeItem(id = testUUIDs[0], deletedAt = Instant.now(OSTestConfig.clock))
        val child = OSStorageTestUtils.createRoomSafeItem(
            id = testUUIDs[1],
            parentId = parent.id,
            deletedParentId = parent.id,
            deletedAt = Instant.now(OSTestConfig.clock),
        )

        safeItemDao.insert(listOf(parent, child))

        ItemOrder.entries.forEach { itemOrder ->
            val expectedRootItems = listOf(parent)
            val actualRootItems = rawDao
                .getPagedItems(SafeItemRawDao.findByDeletedParentIdQuery(null, itemOrder))
                .data()
            assertContentEquals(expectedRootItems, actualRootItems)

            val expectedChildItems = listOf(child)
            val actualChildItems = rawDao
                .getPagedItems(
                    SafeItemRawDao.findByDeletedParentIdQuery(
                        parent.id,
                        itemOrder,
                    ),
                ).data()
            assertContentEquals(expectedChildItems, actualChildItems, itemOrder)
        }
    }

    @Test
    fun findFavoriteQuery_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA1, itemB)
            val actual = rawDao.getItems(SafeItemRawDao.findFavoriteQuery(itemOrder))
            assertContentEquals(expected, actual, itemOrder)
        }
    }

    @Test
    fun findLastFavoriteQuery_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA1, itemB).sortedBy(itemOrder).first()
            val actual = rawDao.getItems(SafeItemRawDao.findLastFavoriteQuery(1, itemOrder)).first()
            assertEquals(expected, actual, "Fail with order = $itemOrder")
        }
    }

    @Test
    fun findByIdWithIdentifierQuery_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA1, itemB).sortedBy(itemOrder)
            val actual = rawDao.getSafeItemsWithIdentifierFlow(
                SafeItemRawDao.findByIdWithIdentifierQuery(listOf(itemA1.id, itemB.id), itemOrder),
            ).first()
            assertContentEqualsIdentifier(expected, actual, itemOrder)
        }
    }

    @Test
    fun findByParentIdWithIdentifierQuery_test(): TestResult = runTest {
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA, itemB).sortedBy(itemOrder)
            val actual = rawDao.getSafeItemsWithIdentifierFlow(
                SafeItemRawDao.findByParentIdWithIdentifierQuery(null, itemOrder),
            ).first()
            assertContentEqualsIdentifier(expected, actual, itemOrder)
        }
    }

    @Test
    fun findByDeletedParentIdWithIdentifierQuery_test(): TestResult = runTest {
        safeItemDao.setDeletedAndRemoveFromFavorite(itemB.id, Instant.now(OSTestConfig.clock))
        safeItemDao.setDeletedAndRemoveFromFavorite(itemA.id, Instant.now(OSTestConfig.clock))
        ItemOrder.entries.forEach { itemOrder ->
            val expected = listOf(itemA, itemB).sortedBy(itemOrder)
            val actual = rawDao.getSafeItemsWithIdentifierFlow(
                SafeItemRawDao.findByDeletedParentIdWithIdentifierQuery(null, itemOrder),
            ).first()
            assertContentEqualsIdentifier(expected, actual, itemOrder)
        }
    }
}

/**
 * Sort according to DaoUtils constants
 */
private fun List<RoomSafeItem>.sortedBy(itemOrder: ItemOrder): List<RoomSafeItem> {
    return when (itemOrder) {
        ItemOrder.Position -> sortedWith(compareBy({ it.position }, { it.indexAlpha }))
        ItemOrder.Alphabetic -> sortedWith(compareBy({ it.indexAlpha }, { it.position }))
        ItemOrder.UpdatedAt -> sortedWith(compareBy({ 1.0 / it.updatedAt.toEpochMilli() }, { it.indexAlpha }, { it.position }))
        ItemOrder.DeletedAt -> sortedWith(compareBy({ it.deletedAt }, { it.indexAlpha }, { it.position }))
        ItemOrder.ConsultedAt -> sortedWith(
            compareBy(
                { 1.0 / (it.consultedAt?.toEpochMilli() ?: Long.MIN_VALUE) },
                { it.indexAlpha },
                { it.position },
            ),
        )
        ItemOrder.CreatedAt -> sortedWith(compareBy({ it.createdAt }, { it.indexAlpha }, { it.position }))
    }
}

private fun assertContentEquals(expected: List<RoomSafeItem>, actual: List<RoomSafeItem>, itemOrder: ItemOrder) {
    assertContentEquals(
        expected = expected.sortedBy(itemOrder),
        actual = actual,
        message = "Fail with order = $itemOrder",
    )
}

private fun assertContentEqualsIdentifier(expected: List<RoomSafeItem>, actual: List<SafeItemWithIdentifier>, itemOrder: ItemOrder) {
    assertContentEquals(
        expected = expected.sortedBy(itemOrder).map(RoomSafeItem::id),
        actual = actual.map(SafeItemWithIdentifier::id),
        message = "Fail with order = $itemOrder",
    )
}
