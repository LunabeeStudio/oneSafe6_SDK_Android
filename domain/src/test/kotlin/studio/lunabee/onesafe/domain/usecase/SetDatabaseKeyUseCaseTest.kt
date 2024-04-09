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
 * Created by Lunabee Studio / Date - 4/2/2024 - for the oneSafe6 SDK.
 * Last modified 4/2/24, 12:45 PM
 */

package studio.lunabee.onesafe.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.usecase.authentication.SetDatabaseKeyUseCase
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.test.DummyDatabaseCryptoRepository
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import kotlin.test.assertEquals

class SetDatabaseKeyUseCaseTest {
    private val databaseKey = DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize))
    private val badKey = DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize))

    private val encryptionManager: DatabaseEncryptionManager = mockk<DatabaseEncryptionManager> {
        every { checkDatabaseAccess(databaseKey) } returns Unit
        every { checkDatabaseAccess(badKey) } throws OSStorageError.Code.DATABASE_WRONG_KEY.get()
    }

    val setDatabaseKeyUseCase: SetDatabaseKeyUseCase = SetDatabaseKeyUseCase(
        databaseKeyRepository = DummyDatabaseCryptoRepository(databaseKey),
        encryptionManager = encryptionManager,
    )

    @Test
    fun set_good_key_test(): TestResult = runTest {
        val actual = setDatabaseKeyUseCase(databaseKey.asCharArray().joinToString(""))
        assertSuccess(actual)
    }

    @Test
    fun set_bad_key_test(): TestResult = runTest {
        val actual = setDatabaseKeyUseCase(badKey.asCharArray().joinToString(""))
        val failure = assertFailure(actual)
        assertEquals(OSStorageError.Code.DATABASE_WRONG_KEY, failure.throwable.osCode())
    }
}
