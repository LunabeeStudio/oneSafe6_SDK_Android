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
 * Created by Lunabee Studio / Date - 6/21/2023 - for the oneSafe6 SDK.
 * Last modified 6/21/23, 5:07 PM
 */

package studio.lunabee.onesafe.storage.dao

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.storage.extension.insert
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class MessageDaoTest {

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var dao: MessageDao

    @Inject internal lateinit var contactDao: ContactDao

    @Inject internal lateinit var safeDao: SafeDao

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            safeDao.insert(CommonTestUtils.roomSafe())
        }
    }

    /**
     * Test getMessageOrderAtByContact is independent from the insertion order and is not impacted by other contact messages
     */
    @Test
    fun getMessageOrderAtByContact_test(): TestResult = runTest {
        val contactId = UUID.randomUUID()
        val contactId2 = UUID.randomUUID()
        val expectedOrder = floatArrayOf(3.5f, 1.3f, 0f)

        contactDao.insert(id = contactId)
        contactDao.insert(id = contactId2)

        dao.insert(id = testUUIDs[0], contactId = contactId, order = expectedOrder[0])
        dao.insert(id = testUUIDs[1], contactId = contactId2, order = expectedOrder[0])
        dao.insert(id = testUUIDs[2], contactId = contactId2, order = expectedOrder[2])
        dao.insert(id = testUUIDs[3], contactId = contactId2, order = expectedOrder[1])
        dao.insert(id = testUUIDs[4], contactId = contactId, order = expectedOrder[2])
        dao.insert(id = testUUIDs[5], contactId = contactId, order = expectedOrder[1])
        dao.insert(id = testUUIDs[6], contactId = contactId, order = 2f) // excluded

        val actualOrder = Array<Float?>(4) { null }
        actualOrder[0] = dao.getMessageOrderAtByContact(0, contactId, listOf(testUUIDs[6]))?.order
        actualOrder[1] = dao.getMessageOrderAtByContact(1, contactId, listOf(testUUIDs[6]))?.order
        actualOrder[2] = dao.getMessageOrderAtByContact(2, contactId, listOf(testUUIDs[6]))?.order
        actualOrder[3] = dao.getMessageOrderAtByContact(3, contactId, listOf(testUUIDs[6]))?.order

        expectedOrder.indices.forEach {
            assertEquals(actualOrder[it], expectedOrder[it])
        }
        assertNull(actualOrder[3])
    }
}
