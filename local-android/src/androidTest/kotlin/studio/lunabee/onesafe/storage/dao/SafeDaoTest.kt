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
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
        // TODO <multisafe> see TODO in InMemoryMainDatabaseModule.provideMainDatabase, ideally we should insert both safe 0 & 1
        val id0 = firstSafeId
        val id1 = SafeId(testUUIDs[1])
        val safe1 = CommonTestUtils.roomSafe(id1)
        dao.insert(safe1)

        dao.setBiometricMaterial(id0, BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64)))
        dao.setBiometricMaterial(id1, BiometricCryptoMaterial(OSTestConfig.random.nextBytes(64)))

        val actual = dao.getAll()

        assertEquals(id1, dao.getBiometricSafe()!!.id)
        assertNull(actual.first { it.id == id0 }.crypto.biometricCryptoMaterial)
        assertNotNull(actual.first { it.id == id1 }.crypto.biometricCryptoMaterial)
    }
}
