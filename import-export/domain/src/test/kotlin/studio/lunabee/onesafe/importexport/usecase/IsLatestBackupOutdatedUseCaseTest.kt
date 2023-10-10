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

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class IsLatestBackupOutdatedUseCaseTest {
    private val settingsRepository: AutoBackupSettingsRepository = mockk {
        every { autoBackupFrequency } returns 1.days
    }
    private val getBackupsUseCase: GetBackupsUseCase = mockk()
    private val nowClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
    private val isLatestBackupOutdatedUseCase: IsLatestBackupOutdatedUseCase = IsLatestBackupOutdatedUseCase(
        getBackupsUseCase = getBackupsUseCase,
        settingsRepository = settingsRepository,
        clock = nowClock,
    )

    @Test
    fun outdated_backup_test() {
        every { getBackupsUseCase.invoke() } returns listOf(
            Backup(
                date = LocalDateTime.now(nowClock).minusDays(6),
                file = mockk(),
            ),
        )
        val actual = isLatestBackupOutdatedUseCase()
        assertTrue(actual)
    }

    @Test
    fun not_outdated_backup_test() {
        every { getBackupsUseCase.invoke() } returns listOf(
            Backup(
                date = LocalDateTime.now(nowClock).minusHours(6),
                file = mockk(),
            ),
        )
        val actual = isLatestBackupOutdatedUseCase()
        assertFalse(actual)
    }

    @Test
    fun no_backup_test() {
        every { getBackupsUseCase.invoke() } returns emptyList()
        val actual = isLatestBackupOutdatedUseCase()
        assertTrue(actual)
    }
}
