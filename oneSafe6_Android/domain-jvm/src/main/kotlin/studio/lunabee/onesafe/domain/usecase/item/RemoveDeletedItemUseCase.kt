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
 * Created by Lunabee Studio / Date - 10/16/2023 - for the oneSafe6 SDK.
 * Last modified 10/9/23, 6:29 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.usecase.DeleteFileAssociatedWithItemsUseCase
import studio.lunabee.onesafe.domain.usecase.DeleteIconUseCase
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

class RemoveDeletedItemUseCase @Inject constructor(
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
    private val deleteIconUseCase: DeleteIconUseCase,
    private val reorderChildrenAtParentLastPositionUseCase: ReorderChildrenAtParentLastPositionUseCase,
    private val deleteFileAssociatedWithItemsUseCase: DeleteFileAssociatedWithItemsUseCase,
) {

    suspend operator fun invoke(
        safeItem: SafeItem,
    ): LBResult<Unit> {
        return OSError.runCatching(
            mapErr = { e -> OSDomainError(OSDomainError.Code.SAFE_ITEM_REMOVE_FAILURE, cause = e) },
        ) {
            // Get original children to update them
            val siblingChildrenItems = safeItemDeletedRepository.getSiblingOriginalChildren(
                parentId = safeItem.id,
                order = ItemOrder.Position,
            )
            siblingChildrenItems.forEach { siblingChild ->
                reorderChildrenAtParentLastPositionUseCase(siblingChild)
            }
            safeItemDeletedRepository.updateSiblingOriginalChildrenParentId(
                parentId = safeItem.id,
                newParentId = safeItem.parentId,
            )

            val deletedItemsToRemove = safeItemDeletedRepository.findDeletedByIdWithDeletedDescendants(safeItem.id)
            val idsToRemove = deletedItemsToRemove.map { it.id }
            deleteFileAssociatedWithItemsUseCase(idsToRemove)

            deleteIconUseCase.invoke(deletedItemsToRemove)
            safeItemDeletedRepository.removeItems(idsToRemove)
        }
    }
}
