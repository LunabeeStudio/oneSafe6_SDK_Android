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
 * Created by Lunabee Studio / Date - 3/26/2024 - for the oneSafe6 SDK.
 * Last modified 3/26/24, 2:36 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.test.OSTestConfig
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ClearAutoBackupErrorUseCaseTest {
    var backupError: AutoBackupError? = null
    private val autoBackupErrorRepository: AutoBackupErrorRepository = DummyAutoBackupErrorRepository()

    inner class DummyAutoBackupErrorRepository : AutoBackupErrorRepository {
        override fun getError(): Flow<AutoBackupError?> = flowOf(backupError)
        override suspend fun setError(error: AutoBackupError?) {
            backupError = error
        }
    }

    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase = mockk {}

    private val useCase = ClearAutoBackupErrorUseCase(
        autoBackupErrorRepository = autoBackupErrorRepository,
        getAutoBackupModeUseCase = getAutoBackupModeUseCase,
    )

    @Test
    fun ifNeeded_clean_disabled_test(): TestResult = runTest {
        val currentErrorMode = AutoBackupMode.entries.random(OSTestConfig.random)
        backupError = AutoBackupError(Instant.EPOCH.atZone(ZoneOffset.UTC), "", "", currentErrorMode)

        coEvery { getAutoBackupModeUseCase.invoke() } returns AutoBackupMode.Disabled
        useCase.ifNeeded(AutoBackupMode.entries.random(OSTestConfig.random))

        assertNull(backupError)
    }

    @Test
    fun ifNeeded_clean_same_mode_as_settings_test(): TestResult = runTest {
        val backupMode = AutoBackupMode.entries.random(OSTestConfig.random)
        backupError = AutoBackupError(Instant.EPOCH.atZone(ZoneOffset.UTC), "", "", backupMode)

        coEvery { getAutoBackupModeUseCase.invoke() } returns backupMode
        useCase.ifNeeded(backupMode)

        assertNull(backupError)
    }

    @Test
    fun ifNeeded_clean_same_mode_as_current_error_test(): TestResult = runTest {
        val backupMode = AutoBackupMode.entries.random(OSTestConfig.random)
        backupError = AutoBackupError(Instant.EPOCH.atZone(ZoneOffset.UTC), "", "", backupMode)

        coEvery { getAutoBackupModeUseCase.invoke() } returns AutoBackupMode.entries
            .filterNot { it == backupMode }
            .random(OSTestConfig.random)
        useCase.ifNeeded(backupMode)

        assertNull(backupError)
    }

    @Test
    fun ifNeeded_keep_test(): TestResult = runTest {
        val backupMode = AutoBackupMode.entries.random(OSTestConfig.random)
        backupError = AutoBackupError(Instant.EPOCH.atZone(ZoneOffset.UTC), "", "", backupMode)
        coEvery { getAutoBackupModeUseCase.invoke() } returns backupMode

        val otherBackupMode = AutoBackupMode.entries
            .filterNot { it == backupMode }
            .random(OSTestConfig.random)
        useCase.ifNeeded(otherBackupMode)

        if (backupMode == AutoBackupMode.Disabled) {
            assertNull(backupError)
        } else {
            assertEquals(backupMode, backupError?.source)
        }
        assertNotEquals(backupMode, otherBackupMode)
    }
}
