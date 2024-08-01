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
 * Last modified 7/24/24, 3:15 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.BiometricCipherRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<DeleteSafeUseCase>()

class DeleteSafeUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val lockAppUseCase: LockAppUseCase,
    private val biometricCipherRepository: BiometricCipherRepository,
    private val deleteBackupsUseCase: DeleteBackupsUseCase,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        lockAppUseCase()

        // Delete files (files, icons, backups)
        iconRepository.deleteAll(safeId)
        fileRepository.deleteAll(safeId)
        deleteBackupsUseCase.invoke(safeId)

        val isBiometricEnabled = safeRepository.isBiometricEnabledForSafe(safeId)
        safeRepository.deleteSafe(safeId)
        if (isBiometricEnabled) {
            biometricCipherRepository.clear()
        }
    }
}
