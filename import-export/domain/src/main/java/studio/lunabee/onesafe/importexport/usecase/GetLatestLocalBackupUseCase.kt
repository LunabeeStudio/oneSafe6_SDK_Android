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
 * Last modified 11/15/23, 3:46 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.importexport.model.LocalBackup
import javax.inject.Inject

/**
 * Get the latest local backup or null if no (valid) local backup
 *
 * @see GetAllLocalBackupsUseCase
 */
class GetLatestLocalBackupUseCase @Inject constructor(
    private val getAllLocalBackupsUseCase: GetAllLocalBackupsUseCase,
) {
    /**
     * @see GetAllLocalBackupsUseCase.invoke
     */
    suspend operator fun invoke(): LocalBackup? = getAllLocalBackupsUseCase().firstOrNull()

    /**
     * @see GetAllLocalBackupsUseCase.flow
     */
    fun flow(): Flow<LocalBackup?> = getAllLocalBackupsUseCase.flow().map { it.firstOrNull() }
}
