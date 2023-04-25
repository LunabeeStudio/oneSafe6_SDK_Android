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
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val log = LBLogger.get<ConfirmPasswordUseCase>()

/**
 * Test password against the temporary on setup key
 */
class ConfirmPasswordUseCase @Inject constructor(
    private val cryptoRepository: EditCryptoRepository,
) {
    suspend operator fun invoke(password: CharArray): LBResult<Unit> {
        val passwordMatch = OSError.runCatching(log) {
            cryptoRepository.checkCryptographicData(password)
        }

        return if (passwordMatch.data == false) {
            LBResult.Failure(OSDomainError(OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD))
        } else {
            LBResult.Success(Unit)
        }
    }
}
