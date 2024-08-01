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
 * Created by Lunabee Studio / Date - 4/18/2024 - for the oneSafe6 SDK.
 * Last modified 4/18/24, 5:15 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.unit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import javax.inject.Inject

/**
 * Force a backup to run according to the user settings. Even if auto backups are disabled, a local backup will be done
 */
class BackupAllSafesUseCase @Inject constructor(
    private val localAutoBackupUseCase: LocalAutoBackupUseCase,
    private val cloudAutoBackupUseCase: CloudAutoBackupUseCase,
    private val synchronizeCloudBackupsUseCase: SynchronizeCloudBackupsUseCase,
    private val getAutoBackupModeUseCase: GetAutoBackupModeUseCase,
    private val safeRepository: SafeRepository,
) {
    operator fun invoke(): Flow<LBFlowResult<Unit>> = flow {
        safeRepository.getAllSafeId().forEach { safeId ->
            val mode = getAutoBackupModeUseCase(safeId)
            when (mode) {
                AutoBackupMode.Disabled,
                AutoBackupMode.LocalOnly,
                -> emitAll(localAutoBackupUseCase(safeId))
                AutoBackupMode.CloudOnly -> emitAll(cloudAutoBackupUseCase(safeId).unit())
                AutoBackupMode.Synchronized -> {
                    emitAll(
                        localAutoBackupUseCase(safeId)
                            .transformResult { emitAll(synchronizeCloudBackupsUseCase(safeId)) }
                            .unit(),
                    )
                }
            }
        }
    }
}
