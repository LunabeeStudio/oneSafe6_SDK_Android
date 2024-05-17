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
 * Created by Lunabee Studio / Date - 4/30/2024 - for the oneSafe6 SDK.
 * Last modified 4/30/24, 9:07 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import javax.inject.Inject

class UpdateAutoBackUpsMaxNumberUseCase @Inject constructor(
    private val settings: AutoBackupSettingsRepository,
    private val deleteOldBackupsUseCase: DeleteOldBackupsUseCase,
) {
    operator fun invoke(maxNumber: Int): Flow<LBFlowResult<Unit>> = flow {
        val previousAutoBackupMaxNumber = settings.autoBackupMaxNumber
        settings.updateAutoBackupMaxNumber(maxNumber)

        if (previousAutoBackupMaxNumber > maxNumber) {
            emitAll(deleteOldBackupsUseCase())
        } else {
            emit(LBFlowResult.Success(Unit))
        }
    }
}
