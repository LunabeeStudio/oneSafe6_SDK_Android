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
 * Created by Lunabee Studio / Date - 9/11/2024 - for the oneSafe6 SDK.
 * Last modified 11/09/2024 16:40
 */

package studio.lunabee.onesafe.domain.usecase.autodestruction

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.use
import javax.inject.Inject

class EnableAutoDestructionUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val mainCryptoRepository: MainCryptoRepository,
) {

    suspend operator fun invoke(password: CharArray): LBResult<Unit> {
        return OSError.runCatching {
            password.use {
                val safeId = safeRepository.currentSafeId()
                val salt = safeRepository.getSalt(safeId)
                val autoDestructionKey = mainCryptoRepository.derivePassword(password = password, salt = salt)
                safeRepository.setAutoDestructionKey(safeId, autoDestructionKey)
            }
        }
    }
}
