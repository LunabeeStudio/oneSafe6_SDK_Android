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

package studio.lunabee.onesafe.domain.common

import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import java.util.UUID

/**
 * Sort initial list by putting parent before children. Also fix potential recursive relationship by nullifying the (deleted) parent id
 */
class SortedAncestors(
    safeItemsNotSorted: List<SafeItem>,
) {
    /**
     * Group elements by parent ID (i.e map of parentId -> all child with the same parentId)
     */
    private val groupedSafeItem: Map<UUID?, List<SafeItem>> = safeItemsNotSorted
        .asSequence()
        .map { item ->
            if (item.id == item.deletedParentId || item.id == item.parentId) {
                item.copy(
                    deletedParentId = item.deletedParentId.takeUnless { it == item.id },
                    parentId = item.parentId.takeUnless { it == item.id },
                )
            } else {
                item
            }
        }.sortedWith(compareBy({ it.parentId }, { it.id }))
        .groupBy { it.parentId }

    /**
     * Starting from elements without parent, loop recursively to create a new list ordered with parent before children.
     * Consult [studio.lunabee.onesafe.domain.utils.SortedAncestorsTest] and explanations here:
     * https://stackoverflow.com/questions/61021088/kotlin-sort-list-of-objects-by-their-id-and-parentid
     */
    fun sortByAncestors(): List<SafeItem> = groupedSafeItem[null]?.flatMap(::sort).orEmpty()

    private fun sort(item: SafeItem): List<SafeItem> = buildList {
        add(item)
        addAll(groupedSafeItem.getOrDefault(item.id, emptyList()).flatMap(::sort))
    }
}
