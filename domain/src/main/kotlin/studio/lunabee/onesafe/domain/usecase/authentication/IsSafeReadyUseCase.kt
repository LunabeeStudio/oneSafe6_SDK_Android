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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSDomainError
import javax.inject.Inject
import kotlin.time.Duration

class IsSafeReadyUseCase @Inject constructor(
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(): Boolean =
        mainCryptoRepository.isCryptoDataInMemory(Duration.ZERO) &&
            kotlin.runCatching { safeRepository.currentSafeId() }.isSuccess

    fun flow(): Flow<Boolean> = safeIdFlow().map { it != null }.distinctUntilChanged()

    fun safeIdFlow(): Flow<SafeId?> = combine(
        mainCryptoRepository.isCryptoDataInMemoryFlow(),
        safeRepository.currentSafeIdFlow(),
    ) { isCryptoLoaded, safeId ->
        if (isCryptoLoaded) {
            safeId
        } else {
            null
        }
    }.distinctUntilChanged()

    suspend fun wait(timeout: Duration = Duration.INFINITE) {
        val isCryptoLoaded = mainCryptoRepository.isCryptoDataInMemory(timeout)
        val isSafeIdLoaded = safeRepository.isSafeIdInMemory(timeout)
        if (!isCryptoLoaded) {
            throw OSDomainError(OSDomainError.Code.CRYPTO_NOT_READY_TIMEOUT)
        }
        if (!isSafeIdLoaded) {
            throw OSDomainError(OSDomainError.Code.SAFE_ID_NOT_READY_TIMEOUT)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> withCrypto(block: Flow<T>): Flow<T> = flow().flatMapLatest { isCryptoLoaded ->
        if (isCryptoLoaded) {
            block
        } else {
            flowOf()
        }
    }
}
