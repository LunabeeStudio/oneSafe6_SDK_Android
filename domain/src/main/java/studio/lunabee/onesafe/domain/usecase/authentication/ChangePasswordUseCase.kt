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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.TransactionManager
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.verifypassword.SetLastPasswordVerificationUseCase
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val log = LBLogger.get<ChangePasswordUseCase>()

class ChangePasswordUseCase @Inject constructor(
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val editCryptoRepository: EditCryptoRepository,
    private val transactionManager: TransactionManager,
    private val setLastPasswordVerificationUseCase: SetLastPasswordVerificationUseCase,
) {
    suspend operator fun invoke(newPassword: CharArray): LBResult<Unit> = OSError.runCatching(logger = log) {
        editCryptoRepository.generateCryptographicData(newPassword)
        transactionManager.withTransaction {
            val keys = safeItemKeyRepository.getAllSafeItemKeys()
            editCryptoRepository.reEncryptItemKeys(keys)
            safeItemKeyRepository.update(keys)
            editCryptoRepository.overrideMainCryptographicData()
            setLastPasswordVerificationUseCase(System.currentTimeMillis())
        }
    }
}
