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
 * Created by Lunabee Studio / Date - 7/24/2024 - for the oneSafe6 SDK.
 * Last modified 7/24/24, 4:52 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteBackupsUseCase
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import javax.inject.Inject

class DeleteBackupsUseCaseImpl @Inject constructor(
    private val localBackupRepository: LocalBackupRepository,
) : DeleteBackupsUseCase {
    override suspend fun invoke(safeId: SafeId) {
        localBackupRepository.deleteAll(safeId)
    }
}
