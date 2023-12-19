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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 31/08/2023 15:18
 */

package studio.lunabee.onesafe.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.SortItemNameUseCase
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Compute alpha index for all items
 */
class MigrationFromV6ToV7 @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoEngine: CryptoEngine,
    private val cryptoDataMapper: CryptoDataMapper,
    private val sortItemNameUseCase: SortItemNameUseCase,
) {
    suspend operator fun invoke(masterKey: ByteArray): LBResult<Unit> = OSError.runCatching {
        val items = safeItemRepository.getAllSafeItemIdName().map { item ->
            val itemKey = safeItemKeyRepository.getSafeItemKey(item.id)
            val itemKeyRaw = cryptoEngine.decrypt(itemKey.encValue, masterKey, null)
            val name = item.encName?.let {
                val plainName = cryptoEngine.decrypt(it, itemKeyRaw, null)
                cryptoDataMapper(null, plainName, String::class)
            } ?: ""
            Pair(item.id, name)
        }
        val sortedItemsId = sortItemNameUseCase(items)
        safeItemRepository.setAlphaIndices(sortedItemsId)
    }
}
