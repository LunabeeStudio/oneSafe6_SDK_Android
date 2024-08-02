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

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

class CountSafeItemUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flow(parentItem: SafeItem): Flow<Int> {
        return safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                if (parentItem.isDeleted) {
                    safeItemDeletedRepository.countSafeItemByParentIdDeletedFlow(parentItem.id, safeId)
                } else {
                    safeItemRepository.countSafeItemByParentIdFlow(parentItem.id, safeId)
                }
            } ?: flowOf(0)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun allDeleted(): Flow<Int> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let { safeItemDeletedRepository.getAllDeletedItemsCount(safeId) } ?: flowOf(0)
    }

    suspend fun deleted(parentId: UUID?): LBResult<Int> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        safeItemDeletedRepository.countSafeItemByParentIdDeleted(parentId, safeId)
    }

    suspend fun notDeleted(parentId: UUID?): LBResult<Int> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        safeItemRepository.countSafeItemByParentId(parentId, safeId)
    }
}
