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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 12:34 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import javax.inject.Inject

/**
 * Setup cloud backup for the current safe using the provided account and synchronize it.
 */
class SetupAndSyncCloudBackupUseCase @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository,
    private val safeRepository: SafeRepository,
) {
    operator fun invoke(accountName: String): Flow<LBFlowResult<List<CloudBackup>>> {
        return flow {
            val safeId = safeRepository.currentSafeId()
            val flow = cloudBackupRepository.setupAccount(accountName, safeId).transformResult {
                emitAll(cloudBackupRepository.refreshBackupList(safeId))
            }
            emitAll(flow)
        }
    }
}
