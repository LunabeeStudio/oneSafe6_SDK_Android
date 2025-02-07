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
 * Created by Lunabee Studio / Date - 10/5/2023 - for the oneSafe6 SDK.
 * Last modified 10/5/23, 10:50 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.test.MockAutoBackupSettingsRepository
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class GetDurationBeforeBackupOutdatedUseCaseTest {
    private val settingsRepository = object : MockAutoBackupSettingsRepository() {
        override suspend fun autoBackupFrequency(safeId: SafeId): Duration {
            check(safeId == firstSafeId)
            return 1.days
        }
    }

    private val getAllLocalBackupsUseCase: GetAllLocalBackupsUseCase = mockk()
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase = mockk {
        coEvery { this@mockk.invoke(firstSafeId) } returns AutoBackupMode.LocalOnly // TODO <AutoBackup> update test with other cases
    }
    private val cloudBackupRepository: CloudBackupRepository = mockk()
    private val nowClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
    private val getDurationBeforeBackupOutdatedUseCase: GetDurationBeforeBackupOutdatedUseCase = GetDurationBeforeBackupOutdatedUseCase(
        getAllLocalBackupsUseCase = getAllLocalBackupsUseCase,
        settingsRepository = settingsRepository,
        clock = nowClock,
        getAutoBackupModeUseCase = getAutoBackupModeUseCase,
        cloudBackupRepository = cloudBackupRepository,
    )

    @Test
    fun outdated_backup_test(): TestResult = runTest {
        coEvery { getAllLocalBackupsUseCase.invoke(firstSafeId) } returns listOf(
            LocalBackup(
                date = LocalDateTime.now(nowClock).minusDays(6).toInstant(ZoneOffset.UTC),
                file = mockk(),
                safeId = firstSafeId,
            ),
        )
        val actual = getDurationBeforeBackupOutdatedUseCase(firstSafeId)
        assertEquals((1 - 6).days, actual)
    }

    @Test
    fun not_outdated_backup_test(): TestResult = runTest {
        coEvery { getAllLocalBackupsUseCase.invoke(firstSafeId) } returns listOf(
            LocalBackup(
                date = LocalDateTime.now(nowClock).minusHours(6).toInstant(ZoneOffset.UTC),
                file = mockk(),
                safeId = firstSafeId,
            ),
        )
        val actual = getDurationBeforeBackupOutdatedUseCase(firstSafeId)
        assertEquals((24 - 6).hours, actual)
    }

    @Test
    fun no_backup_test(): TestResult = runTest {
        coEvery { getAllLocalBackupsUseCase.invoke(firstSafeId) } returns emptyList()
        val actual = getDurationBeforeBackupOutdatedUseCase(firstSafeId)
        assertEquals(Duration.ZERO, actual)
    }
}
