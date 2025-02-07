/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/24/2024 - for the oneSafe6 SDK.
 * Last modified 7/24/24, 9:15 AM
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
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class SafeDaoTest {
    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var dao: SafeDao

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun check_biometric_key_uniqueness_trigger_test(): TestResult = runTest {
        val safe0 = CommonTestUtils.roomSafe(firstSafeId)
        val id1 = SafeId(testUUIDs[1])
        val safe1 = CommonTestUtils.roomSafe(id1)
        dao.insert(safe0)
        dao.insert(safe1)

        dao.setBiometricMaterial(firstSafeId, BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64)))
        dao.setBiometricMaterial(id1, BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64)))

        val actual = dao.getAllOrderByLastOpenAsc()

        assertEquals(id1, dao.getBiometricSafe()!!.id)
        assertNull(actual.first { it.id == firstSafeId }.crypto.biometricCryptoMaterial)
        assertNotNull(actual.first { it.id == id1 }.crypto.biometricCryptoMaterial)
    }

    @Test
    fun last_open_insertion_test(): TestResult = runTest {
        val ids = insertSafes()

        // Check safe order after multi insert
        val actualIdsOrdered = dao.getAllOrderByLastOpenAsc().map { it.id }
        assertContentEquals(ids.reversed(), actualIdsOrdered)
    }

    @Test
    fun last_open_delete_test(): TestResult = runTest {
        val ids = insertSafes().reversed()

        // Check re-ordering after delete
        dao.delete(ids[4])
        val safesAfterDelete = dao.getAllOrderByLastOpenAsc()
        assertEquals(ids[5], safesAfterDelete[4].id) // 5 become 4
        assertEquals(ids[3], safesAfterDelete[3].id) // safes before 4 does not change
    }

    @Test
    fun last_open_set_last_open_test(): TestResult = runTest {
        val ids = insertSafes().reversed()

        // Check re-order after set last open
        dao.setLastOpen(ids[6])
        val safesAfterSetLastOpen = dao.getAllOrderByLastOpenAsc()
        assertEquals(ids[6], safesAfterSetLastOpen.first().id) // new 0
        assertEquals(ids[0], safesAfterSetLastOpen[1].id) // old 0 become 1
        assertEquals(ids[7], safesAfterSetLastOpen[7].id) // safes after 6 does not change
    }

    private suspend fun insertSafes(): List<SafeId> = testUUIDs
        .take(10)
        .map {
            val id = SafeId(it)
            val safe = CommonTestUtils.roomSafe(id)
            dao.insert(safe)
            id
        }
}
