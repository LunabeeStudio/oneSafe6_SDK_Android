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
 * Created by Lunabee Studio / Date - 9/23/2024 - for the oneSafe6 SDK.
 * Last modified 23/09/2024 09:36
 */

package studio.lunabee.onesafe.domain.usecase.autodestruction

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.repository.WorkerCryptoRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.toByteArray

class EncryptPasswordAutoDestructionUseCase @Inject constructor(
    private val workerCryptoRepository: WorkerCryptoRepository,
) {
    suspend operator fun invoke(password: CharArray): LBResult<ByteArray> {
        return OSError.runCatching {
            workerCryptoRepository.encrypt(password.toByteArray())
        }
    }
}
