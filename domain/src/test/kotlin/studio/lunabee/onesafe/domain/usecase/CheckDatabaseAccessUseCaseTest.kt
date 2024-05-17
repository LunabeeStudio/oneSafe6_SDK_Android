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
 * Last modified 4/2/24, 11:50 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.usecase.authentication.CheckDatabaseAccessUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.test.DummyDatabaseCryptoRepository
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import kotlin.test.assertEquals

class CheckDatabaseAccessUseCaseTest {

    private val databaseKey = DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize))
    private val badKey = DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize))

    private val encryptionManager: DatabaseEncryptionManager = mockk<DatabaseEncryptionManager> {
        every { checkDatabaseAccess(databaseKey) } returns Unit
        every { checkDatabaseAccess(badKey) } throws OSStorageError.Code.DATABASE_WRONG_KEY.get()
    }

    private val databaseKeyRepository: DummyDatabaseCryptoRepository = DummyDatabaseCryptoRepository(databaseKey)

    val useCase: CheckDatabaseAccessUseCase = CheckDatabaseAccessUseCase(
        encryptionManager = encryptionManager,
        databaseKeyRepository = databaseKeyRepository,
    )

    @BeforeEach
    fun setUp(): TestResult = runTest {
        databaseKeyRepository.setKey(databaseKey, true)
    }

    @Test
    fun check_good_key_test(): TestResult = runTest {
        val actual = useCase()
        assertSuccess(actual)
    }

    @Test
    fun check_bad_key_test(): TestResult = runTest {
        databaseKeyRepository.setKey(badKey, true)
        val actual: LBResult<Unit> = useCase()
        val failure: LBResult.Failure<Unit> = assertFailure(actual)
        assertEquals(OSStorageError.Code.DATABASE_WRONG_KEY, failure.throwable.osCode())
    }

    @Test
    fun check_key_throw_test(): TestResult = runTest {
        databaseKeyRepository.throwInKeyFlow = OSCryptoError(code = OSCryptoError.Code.DATASTORE_KEY_PERMANENTLY_INVALIDATE)
        databaseKeyRepository.setKey(badKey, override = true)
        val actual: LBResult<Unit> = useCase()
        val failure: LBResult.Failure<Unit> = assertFailure(actual)
        assertEquals(OSCryptoError.Code.DATASTORE_KEY_PERMANENTLY_INVALIDATE, failure.throwable.osCode())
    }

    @Test
    fun check_no_db_test(): TestResult = runTest {
        every { encryptionManager.checkDatabaseAccess(databaseKey) } throws OSStorageError.Code.DATABASE_NOT_FOUND.get()

        val actual = useCase()
        assertFailure(actual)

        databaseKeyRepository.removeKey()
        every { encryptionManager.checkDatabaseAccess(null) } throws OSStorageError.Code.DATABASE_NOT_FOUND.get()
        val actualNoKey = useCase()
        assertSuccess(actualNoKey)
    }
}
