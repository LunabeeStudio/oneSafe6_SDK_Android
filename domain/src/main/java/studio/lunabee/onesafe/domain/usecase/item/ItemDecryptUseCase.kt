/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 5/24/23, 8:39 AM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * Decrypt any item or item field data
 */
class ItemDecryptUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) {

    /**
     * Decrypt the raw encrypted data to type [Data]. If the crypto stuff is not loaded, wait 500ms before returning an error.
     *
     * @param data The data to decrypt
     * @param itemId The ID of the linked item
     * @param clazz The output type expected
     *
     * @return Plain data wrapped in a [LBResult]
     */
    suspend operator fun <Data : Any> invoke(data: ByteArray, itemId: UUID, clazz: KClass<Data>): LBResult<Data> {
        return OSError.runCatching {
            val key = safeItemKeyRepository.getSafeItemKey(itemId)
            isCryptoDataReadyInMemoryUseCase.wait(500.milliseconds)
            cryptoRepository.decrypt(key, DecryptEntry(data, clazz))
        }
    }
}
