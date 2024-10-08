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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 8:54 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import studio.lunabee.onesafe.jvm.data
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import javax.inject.Inject

/**
 * Delete oldest local backups to keep only the number required by the user
 */
class DeleteOldLocalBackupsUseCase @Inject constructor(
    private val backupRepository: LocalBackupRepository,
    private val autoBackupSettingsRepository: AutoBackupSettingsRepository,
) {
    suspend operator fun invoke(safeId: SafeId) {
        val oldBackups = backupRepository.getBackups(safeId)
            .drop(autoBackupSettingsRepository.autoBackupMaxNumber(safeId))
        backupRepository.delete(oldBackups.data.filterNotNull(), safeId)
    }
}
