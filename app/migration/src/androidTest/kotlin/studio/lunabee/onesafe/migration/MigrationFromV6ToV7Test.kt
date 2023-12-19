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
 * Created by Lunabee Studio / Date - 12/8/2023 - for the oneSafe6 SDK.
 * Last modified 12/8/23, 11:36 AM
 */

package studio.lunabee.onesafe.migration

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.SaltProvider
import studio.lunabee.onesafe.domain.usecase.item.CleanForAlphaIndexingUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.test
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class MigrationFromV6ToV7Test : OSHiltTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    private val salt: ByteArray = OSTestUtils.random.nextBytes(32)

    @Inject lateinit var hashEngine: PasswordHashEngine

    @BindValue
    val saltProvider: SaltProvider = mockk {
        every { this@mockk.invoke(any()) } returns salt.copyOf()
    }

    private suspend fun masterKey(): ByteArray = hashEngine.deriveKey(testPassword.toCharArray(), salt)

    @Inject lateinit var migrationFromV6ToV7: MigrationFromV6ToV7

    @Inject lateinit var safeItemDao: SafeItemDao

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var cleanForAlphaIndexingUseCase: CleanForAlphaIndexingUseCase

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Test
    fun run_migration_v6_v7_test(): TestResult = runTest {
        val expectedNameIndex = listOf(
            "🥹〄🪂😶‍🌫️" to 0.0,
            "🦊 123 ?.🐾:õ雪abc" to 1.0,
            "123abcABC️" to 2.0,
            "123AbCaBc️" to 2.0,
            "@# &()!" to 3.0,
            "éàçÄйά" to 4.0,
            "ライカ Райка Ράικα" to 5.0,
        )

        val idName = expectedNameIndex
            .shuffled(OSTestUtils.random) // Do not insert in the correct order
            .associate { nameIndex ->
                val plainName = nameIndex.first
                val item = createItemUseCase.test(
                    name = plainName,
                )
                safeItemDao.setAlphaIndex(item.id, 0.0) // simulate state after Room migration 7->8
                plainName to item.id
            }

        val expectedList = expectedNameIndex.map {
            idName[it.first]!! to it.second
        }

        migrationFromV6ToV7(masterKey())

        val actualList = safeItemDao.getAllSafeItems()
        expectedList.forEach { expected ->
            val actual = actualList.first { it.id == expected.first }
            assertEquals(
                expected = expected.second,
                actual = actual.indexAlpha,
                message = "Expected ${idName.entries.first { it.value == expected.first }.key}, got ${
                    decryptUseCase(
                        data = actual.encName!!,
                        itemId = actual.id,
                        clazz = String::class,
                    ).data!!
                }.",
            )
        }
    }
}
