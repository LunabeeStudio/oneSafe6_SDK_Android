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
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.use
import javax.inject.Inject

private val log = LBLogger.get<GenerateCryptoForNewSafeUseCase>()

// TODO <multisafe> unit test

/**
 * Generate cryptographic stuff from a password to create a new safe.
 *  • takes care of verifying the password uniqueness across every safes
 */
class GenerateCryptoForNewSafeUseCase @Inject constructor(
    private val editCryptoRepository: EditCryptoRepository,
) {
    suspend operator fun invoke(password: CharArray): LBResult<CreateMasterKeyResult> = OSError.runCatching(log) {
        password.use {
            val isNotUsed = editCryptoRepository.checkPasswordUniqueness(password)
            if (isNotUsed) {
                editCryptoRepository.generateCryptographicData(password)
                CreateMasterKeyResult.Ok
            } else {
                CreateMasterKeyResult.AlreadyExist
            }
        }
    }
}

enum class CreateMasterKeyResult {
    Ok,
    AlreadyExist,
}
