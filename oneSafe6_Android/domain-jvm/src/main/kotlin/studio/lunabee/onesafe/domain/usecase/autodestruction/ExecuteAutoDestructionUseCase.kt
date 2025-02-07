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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 16:11
 */

package studio.lunabee.onesafe.domain.usecase.autodestruction

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.toCharArray

private val logger = LBLogger.get<ExecuteAutoDestructionUseCase>()

class ExecuteAutoDestructionUseCase @Inject constructor(
    private val deleteSafeUseCase: DeleteSafeUseCase,
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) {

    suspend operator fun invoke(password: ByteArray): LBResult<Unit> = OSError.runCatching(logger) {
        safeRepository.getAllSafeOrderByLastOpenAsc().forEach { safeCrypto ->
            safeCrypto.autoDestructionKey?.let { key ->
                if (mainCryptoRepository.derivePassword(safeCrypto.salt, password.toCharArray()).contentEquals(key)) {
                    deleteSafeUseCase(safeCrypto.id)
                }
            }
        }
    }
}
