/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 2:46 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// TODO <AutoBackup> update tests

/**
 * Check if the latest backup is outdated regarding the frequency param and the backup mode. In case of [AutoBackupMode.Synchronized] mode,
 * check get the older backups between the latest local and latest cloud.
 */
class GetDurationBeforeBackupOutdatedUseCase @Inject constructor(
    private val getAllLocalBackupsUseCase: GetAllLocalBackupsUseCase,
    private val settingsRepository: AutoBackupSettingsRepository,
    private val clock: Clock,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val cloudBackupRepository: CloudBackupRepository,
) {
    suspend operator fun invoke(safeId: SafeId): Duration {
        val backupMode = getAutoBackupModeUseCase(safeId)
        return when (backupMode) {
            AutoBackupMode.Disabled -> Duration.INFINITE
            AutoBackupMode.LocalOnly -> {
                getAllLocalBackupsUseCase(safeId).maxOrNull()?.let { latestBackup ->
                    val durationSinceLatest = latestBackup.date.until(Instant.now(clock), ChronoUnit.SECONDS).seconds
                    settingsRepository.autoBackupFrequency(safeId) - durationSinceLatest
                }
            }
            AutoBackupMode.CloudOnly -> {
                cloudBackupRepository.getBackups(safeId).maxOrNull()?.let { latestBackup ->
                    val durationSinceLatest = latestBackup.date.until(Instant.now(clock), ChronoUnit.SECONDS).seconds
                    settingsRepository.autoBackupFrequency(safeId) - durationSinceLatest
                }
            }
            AutoBackupMode.Synchronized -> {
                cloudBackupRepository.getBackups(safeId).maxOrNull()?.date?.let { latestCloud ->
                    getAllLocalBackupsUseCase(safeId).maxOrNull()?.date?.let { latestLocal ->
                        val durationSinceLatest = minOf(latestCloud, latestLocal)
                            .until(Instant.now(clock), ChronoUnit.SECONDS).seconds
                        settingsRepository.autoBackupFrequency(safeId) - durationSinceLatest
                    }
                }
            }
        } ?: Duration.ZERO
    }
}
