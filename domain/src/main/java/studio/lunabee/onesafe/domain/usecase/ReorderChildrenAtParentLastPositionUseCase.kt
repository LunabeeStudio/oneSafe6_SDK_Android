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

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import javax.inject.Inject

/**
 * Update children item position after parent last position
 */
class ReorderChildrenAtParentLastPositionUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
) {
    suspend operator fun invoke(safeItem: SafeItem) {
        val items = if (safeItem.isDeleted) {
            safeItemDeletedRepository.getDeletedItemsByDeletedParent(safeItem.id)
        } else {
            safeItemRepository.getChildren(safeItem.id)
        }
        val minPos = items.firstOrNull()?.position
        minPos?.let {
            val highestPosition = safeItemRepository.getHighestChildPosition(safeItem.parentId) ?: 0.0
            if (highestPosition >= minPos) {
                items.forEachIndexed { idx, item ->
                    safeItemRepository.updateSafeItem(
                        item.copy(
                            position = highestPosition + idx + 1.0,
                        ),
                        null, // No index update required
                    )
                }
            }
        }
    }
}
