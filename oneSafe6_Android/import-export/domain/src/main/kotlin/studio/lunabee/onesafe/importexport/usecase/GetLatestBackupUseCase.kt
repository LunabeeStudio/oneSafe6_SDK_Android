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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 11/15/23, 3:41 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import javax.inject.Inject

/**
 * Retrieve the last auto-backup (local + remote)
 */
class GetLatestBackupUseCase @Inject constructor(
    private val getLatestLocalBackupUseCase: GetLatestLocalBackupUseCase,
    private val cloudBackupRepository: CloudBackupRepository,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val safeRepository: SafeRepository,
) {
    /**
     * Get all distinct & sorted backups
     */
    suspend operator fun invoke(): LBResult<LatestBackups?> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        val mode = getAutoBackupModeUseCase(safeId)
        when (mode) {
            AutoBackupMode.Disabled -> null
            AutoBackupMode.LocalOnly -> getLatestLocalBackupUseCase()?.let { LatestBackups(it, null) }
            AutoBackupMode.CloudOnly -> cloudBackupRepository.getLatestBackup(safeId)?.let { LatestBackups(null, it) }
            AutoBackupMode.Synchronized -> {
                val localBackup = getLatestLocalBackupUseCase()
                val cloudBackup = cloudBackupRepository.getLatestBackup(safeId)
                LatestBackups(localBackup, cloudBackup)
            }
        }
    }

    /**
     * Get a flow of all all distinct & sorted backups
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flow(currentSafeId: SafeId? = null): Flow<LatestBackups?> {
        return currentSafeId?.let { safeId -> latestBackupsFlow(safeId) }
            ?: safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
                safeId?.let { latestBackupsFlow(safeId) } ?: flowOf(null)
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun latestBackupsFlow(safeId: SafeId) = getAutoBackupModeUseCase.flow(safeId).flatMapLatest { mode ->
        when (mode) {
            AutoBackupMode.Disabled -> flowOf(null)
            AutoBackupMode.LocalOnly -> getLatestLocalBackupUseCase.flow().map { LatestBackups(it, null) }
            AutoBackupMode.CloudOnly -> cloudBackupRepository.getLatestBackupFlow(safeId).map { LatestBackups(null, it) }
            AutoBackupMode.Synchronized -> combine(
                getLatestLocalBackupUseCase.flow(),
                cloudBackupRepository.getLatestBackupFlow(safeId),
            ) { localBackup, cloudBackup ->
                LatestBackups(localBackup, cloudBackup)
            }
        }
    }
}
