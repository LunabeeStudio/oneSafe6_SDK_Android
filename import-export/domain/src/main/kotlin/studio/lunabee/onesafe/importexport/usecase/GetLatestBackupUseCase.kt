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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import javax.inject.Inject

/**
 * Retrieve the last auto-backup (local + remote)
 */
class GetLatestBackupUseCase @Inject constructor(
    private val getLatestLocalBackupUseCase: GetLatestLocalBackupUseCase,
    private val cloudBackupRepository: CloudBackupRepository,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
) {
    /**
     * Get all distinct & sorted backups
     */
    suspend operator fun invoke(): Backup? {
        val mode = getAutoBackupModeUseCase()
        return when (mode) {
            AutoBackupMode.DISABLED -> null
            AutoBackupMode.LOCAL_ONLY -> getLatestLocalBackupUseCase()
            AutoBackupMode.CLOUD_ONLY -> cloudBackupRepository.getLatestBackup()
            AutoBackupMode.SYNCHRONIZED -> {
                val localBackup = getLatestLocalBackupUseCase()
                val cloudBackup = cloudBackupRepository.getLatestBackup()
                return filterResult(localBackup, cloudBackup)
            }
        }
    }

    /**
     * Get a flow of all all distinct & sorted backups
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flow(): Flow<Backup?> {
        return getAutoBackupModeUseCase.flow().flatMapLatest { mode ->
            when (mode) {
                AutoBackupMode.DISABLED -> flowOf(null)
                AutoBackupMode.LOCAL_ONLY -> getLatestLocalBackupUseCase.flow()
                AutoBackupMode.CLOUD_ONLY -> cloudBackupRepository.getLatestBackupFlow()
                AutoBackupMode.SYNCHRONIZED -> combine(
                    getLatestLocalBackupUseCase.flow(),
                    cloudBackupRepository.getLatestBackupFlow(),
                ) { localBackup, cloudBackup ->
                    filterResult(localBackup, cloudBackup)
                }
            }
        }
    }

    private fun filterResult(localBackup: LocalBackup?, cloudBackup: CloudBackup?): Backup? {
        return localBackup?.let {
            cloudBackup?.let {
                maxOf(localBackup, cloudBackup)
            } ?: localBackup
        } ?: cloudBackup
    }
}
