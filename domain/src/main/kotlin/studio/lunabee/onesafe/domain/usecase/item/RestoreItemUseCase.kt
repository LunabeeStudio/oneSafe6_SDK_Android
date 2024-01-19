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
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

/**
 * Restore an item with its descendants. The item will be move to its first non deleted parent (or home if none). Calling this use case with
 * null means restore all items.
 */
class RestoreItemUseCase @Inject constructor(
    private val safeItemDeletedRepository: SafeItemDeletedRepository,
) {
    suspend operator fun invoke(
        item: SafeItem,
    ): LBResult<Unit> = invoke(item.id)

    suspend operator fun invoke(
        itemId: UUID?,
    ): LBResult<Unit> {
        return OSError.runCatching {
            safeItemDeletedRepository.restoreItemToParentWithDescendants(itemId)
            itemId?.let { safeItemDeletedRepository.updateParentToNonDeletedAncestor(itemId) }
        }
    }
}
