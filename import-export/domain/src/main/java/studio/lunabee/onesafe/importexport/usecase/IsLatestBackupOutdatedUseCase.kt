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

import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Check if the latest backup is outdated against frequency param
 */
class IsLatestBackupOutdatedUseCase @Inject constructor(
    private val getBackupsUseCase: GetBackupsUseCase,
    private val settingsRepository: AutoBackupSettingsRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Boolean {
        val latestBackup = getBackupsUseCase().firstOrNull() ?: return true
        val durationSinceLatest = latestBackup.date.until(LocalDateTime.now(clock), ChronoUnit.SECONDS).seconds
        return durationSinceLatest >= settingsRepository.autoBackupFrequency
    }
}
