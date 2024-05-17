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
 * Created by Lunabee Studio / Date - 4/24/2024 - for the oneSafe6 SDK.
 * Last modified 4/24/24, 5:44 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject

class DeleteOldBackupsUseCase @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val deleteOldCloudBackupsUseCase: DeleteOldCloudBackupsUseCase,
    private val deleteOldLocalBackupsUseCase: DeleteOldLocalBackupsUseCase,
) {
    operator fun invoke(): Flow<LBFlowResult<Unit>> = flow {
        val keepLocalBackupEnabled = settings.keepLocalBackupEnabled.first()
        val cloudBackupEnabled = settings.cloudBackupEnabled.first()

        if (keepLocalBackupEnabled) {
            deleteOldLocalBackupsUseCase()
        }

        if (cloudBackupEnabled) {
            emitAll(deleteOldCloudBackupsUseCase())
        } else {
            emit(LBFlowResult.Success(Unit))
        }
    }
}
