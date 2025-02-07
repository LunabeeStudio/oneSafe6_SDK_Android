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
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.StorageManager
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.use
import javax.inject.Inject

private val log = LBLogger.get<ChangePasswordUseCase>()

class ChangePasswordUseCase @Inject constructor(
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val editCryptoRepository: EditCryptoRepository,
    private val transactionManager: StorageManager,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(newPassword: CharArray): LBResult<Unit> = OSError.runCatching(logger = log) {
        newPassword.use {
            editCryptoRepository.generateCryptographicData(newPassword)
        }
        val safeId = safeRepository.currentSafeId()
        transactionManager.withTransaction {
            val keys = safeItemKeyRepository.getAllSafeItemKeys(safeId)
            val autoDestructionKey = safeRepository.getSafeCrypto(safeId)?.autoDestructionKey
            editCryptoRepository.reEncryptItemKeys(keys)
            safeItemKeyRepository.update(keys)
            val cryptoSafe = editCryptoRepository.overrideMainCryptographicData(safeId)
            val safeCrypto = SafeCrypto(
                id = safeId,
                salt = cryptoSafe.salt,
                encTest = cryptoSafe.encTest,
                encIndexKey = cryptoSafe.encIndexKey,
                encBubblesKey = cryptoSafe.encBubblesKey,
                encItemEditionKey = cryptoSafe.encItemEditionKey,
                biometricCryptoMaterial = null,
                autoDestructionKey = autoDestructionKey,
            )
            safeRepository.updateSafeCrypto(safeCrypto = safeCrypto)
            setSecuritySettingUseCase.setLastPasswordVerification(currentSafeId = safeId)
        }
    }
}
