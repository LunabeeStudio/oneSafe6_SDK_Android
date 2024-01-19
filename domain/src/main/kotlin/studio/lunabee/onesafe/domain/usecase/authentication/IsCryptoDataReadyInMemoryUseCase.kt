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

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSDomainError
import javax.inject.Inject
import kotlin.time.Duration

class IsCryptoDataReadyInMemoryUseCase @Inject constructor(
    private val mainCryptoRepository: MainCryptoRepository,
) {
    operator fun invoke(): Boolean = mainCryptoRepository.isCryptoDataInMemory()

    fun flow(): Flow<Boolean> = mainCryptoRepository.isCryptoDataInMemoryFlow()

    @OptIn(FlowPreview::class)
    suspend fun wait(timeout: Duration? = null) {
        val flow = flow().filter { it }
        if (timeout == null) {
            flow.first()
        } else {
            try {
                flow.timeout(timeout).first()
            } catch (e: TimeoutCancellationException) {
                throw OSDomainError(OSDomainError.Code.CRYPTO_NOT_READY_TIMEOUT, cause = e)
            }
        }
    }
}
