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

package studio.lunabee.onesafe.storage.database

import android.database.sqlite.SQLiteConstraintException
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.OSStorageTestUtils
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.toRoomUpdateSafeItem
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNull

@HiltAndroidTest
class MainDatabaseTest {
    @Inject
    internal lateinit var safeItemDao: SafeItemDao

    @Inject
    internal lateinit var db: MainDatabase

    @get:Rule
    var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun createDb() {
        hiltRule.inject()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetSafeItem() {
        val roomSafeItem: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem()
        runTest {
            safeItemDao.insert(roomSafeItem)
            val actualItem = safeItemDao.findById(roomSafeItem.id)
            assertEquals(roomSafeItem, actualItem)
        }
    }

    @Test
    fun insertAndFindSafeItemById() {
        val safeItem: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem(position = 1.0)
        var lastIdx = 0
        runTest {
            safeItemDao.findByIdAsFlow(safeItem.id).take(3).collectIndexed { index, value ->
                when (index) {
                    0 -> {
                        assertNull(value)
                        safeItemDao.insert(safeItem)
                    }
                    1 -> {
                        assertEquals(1.0, value?.position)
                        safeItemDao.update(safeItem.copy(position = 2.0).toRoomUpdateSafeItem())
                    }
                    2 -> assertEquals(2.0, value?.position)
                }
                lastIdx = index
            }
        }
        assertEquals(2, lastIdx)
    }

    @Test
    fun cascadeDeleteItems() {
        val parentSafeItem: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem()
        val childSafeItem: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem(parentId = parentSafeItem.id)
        runTest {
            safeItemDao.insert(listOf(parentSafeItem, childSafeItem))
            assertEquals(2, safeItemDao.findByIdWithDescendants(parentSafeItem.id).size)
            safeItemDao.removeById(parentSafeItem.id)
            assertEquals(0, safeItemDao.findByIdWithDescendants(parentSafeItem.id).size)
        }
    }

    /**
     * Test recursive triggers on parent_id and deleted_parent_id
     */
    @Test
    fun recursive_triggers_test(): TestResult = runTest {
        val id0 = testUUIDs[0]
        val id1 = testUUIDs[1]
        val id2 = testUUIDs[2]

        val item0: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem(id = id0, parentId = id0)
        val item1: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem(id = id1, deletedParentId = id1)
        val item2: RoomSafeItem = OSStorageTestUtils.createRoomSafeItem(id = id2)

        safeItemDao.insert(item2)

        assertThrows<SQLiteConstraintException> {
            safeItemDao.insert(item0)
        }.let { println(it.message) }

        assertThrows<SQLiteConstraintException> {
            safeItemDao.insert(item1)
        }.let { println(it.message) }

        assertThrows<SQLiteConstraintException> {
            safeItemDao.update(item2.copy(parentId = item2.id).toRoomUpdateSafeItem())
        }.let { println(it.message) }

        assertThrows<SQLiteConstraintException> {
            safeItemDao.update(item2.copy(deletedParentId = item2.id).toRoomUpdateSafeItem())
        }.let { println(it.message) }
    }
}
