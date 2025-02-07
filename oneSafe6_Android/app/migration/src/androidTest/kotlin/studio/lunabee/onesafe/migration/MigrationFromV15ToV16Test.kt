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
 * Created by Lunabee Studio / Date - 3/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/03/2024 09:05
 */

package studio.lunabee.onesafe.migration

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.utils.SaltProvider
import studio.lunabee.onesafe.migration.migration.MigrationFromV15ToV16
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.assertContentNotEquals
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class MigrationFromV15ToV16Test : OSHiltTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject lateinit var migrationFromV15ToV16: MigrationFromV15ToV16

    @Inject lateinit var mainCryptoRepository: MainCryptoRepository

    private val salt: ByteArray = OSTestConfig.random.nextBytes(32)

    @Inject lateinit var hashEngine: PasswordHashEngine

    @BindValue
    val saltProvider: SaltProvider = mockk {
        every { this@mockk.invoke(any()) } returns salt.copyOf()
    }

    private val masterKey: ByteArray by lazy { runBlocking { hashEngine.deriveKey(testPassword.toCharArray(), salt) } }

    @Test
    fun run_migrationFromV15ToV16_bubbles_null_key_test(): TestResult = runTest {
        val cryptoSafe = mainCryptoRepository.generateCrypto(
            key = masterKey,
            salt = salt,
            biometricCipher = null,
        )
        val wrongBubblesKey = byteArrayOf(-1)
        val safeCrypto = SafeCrypto(
            id = firstSafeId,
            salt = cryptoSafe.salt,
            encTest = cryptoSafe.encTest,
            encIndexKey = cryptoSafe.encIndexKey,
            encBubblesKey = wrongBubblesKey,
            encItemEditionKey = cryptoSafe.encItemEditionKey,
            biometricCryptoMaterial = null,
            autoDestructionKey = null,
        )
        safeRepository.insertSafe(
            safeCrypto = safeCrypto,
            safeSettings = OSTestUtils.safeSettings(),
            appVisit = OSTestUtils.appVisit(),
        )
        val migrationSafeData15 = AppMigrationsTestUtils.safeData15(
            masterKey = masterKey,
            salt = salt,
            encBubblesKey = null,
        )
        migrationFromV15ToV16.migrate(migrationSafeData15)
        val actualSafeCrypto = safeRepository.getSafeCrypto(firstSafeId)!!

        // Bubbles key has been generated
        assertContentNotEquals(wrongBubblesKey, actualSafeCrypto.encBubblesKey)
    }

    @Test
    fun run_migrationFromV15ToV16_bubbles_not_null_key_test(): TestResult = runTest {
        val cryptoSafe = mainCryptoRepository.generateCrypto(
            key = masterKey,
            salt = salt,
            biometricCipher = null,
        )
        val safeCrypto = SafeCrypto(
            id = firstSafeId,
            salt = cryptoSafe.salt,
            encTest = cryptoSafe.encTest,
            encIndexKey = cryptoSafe.encIndexKey,
            encBubblesKey = cryptoSafe.encBubblesKey,
            encItemEditionKey = cryptoSafe.encItemEditionKey,
            biometricCryptoMaterial = null,
            autoDestructionKey = null,
        )
        safeRepository.insertSafe(
            safeCrypto = safeCrypto,
            safeSettings = OSTestUtils.safeSettings(),
            appVisit = OSTestUtils.appVisit(),
        )
        val migrationSafeData15 = AppMigrationsTestUtils.safeData15(
            masterKey = masterKey,
            salt = salt,
            encBubblesKey = cryptoSafe.encBubblesKey,
        )
        migrationFromV15ToV16.migrate(migrationSafeData15)
        val actualSafeCrypto = safeRepository.getSafeCrypto(firstSafeId)!!

        // Bubbles key untouched
        assertContentEquals(cryptoSafe.encBubblesKey, actualSafeCrypto.encBubblesKey)
    }
}
