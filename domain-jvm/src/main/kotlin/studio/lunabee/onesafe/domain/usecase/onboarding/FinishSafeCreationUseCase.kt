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

package studio.lunabee.onesafe.domain.usecase.onboarding

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.common.SafeIdProvider
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Load & persist the temporary key of the edit repository. The use case is safe to call twice.
 */
class FinishSafeCreationUseCase @Inject constructor(
    private val editCryptoRepository: EditCryptoRepository,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
    private val safeIdProvider: SafeIdProvider,
    private val safeRepository: SafeRepository,
    private val getDefaultSafeSettingsUseCase: DefaultSafeSettingsProvider,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching {
        val cryptoSafe = editCryptoRepository.setMainCryptographicData()
        val safeId = safeIdProvider()
        val safeCrypto = SafeCrypto(
            id = safeId,
            salt = cryptoSafe.salt,
            encTest = cryptoSafe.encTest,
            encIndexKey = cryptoSafe.encIndexKey,
            encBubblesKey = cryptoSafe.encBubblesKey,
            encItemEditionKey = cryptoSafe.encItemEditionKey,
            biometricCryptoMaterial = cryptoSafe.biometricCryptoMaterial,
            autoDestructionKey = null,
        )
        safeRepository.insertSafe(
            safeCrypto = safeCrypto,
            safeSettings = getDefaultSafeSettingsUseCase(),
            appVisit = AppVisit(),
        )
        setSecuritySettingUseCase.setLastPasswordVerification(currentSafeId = safeId)
    }
}
