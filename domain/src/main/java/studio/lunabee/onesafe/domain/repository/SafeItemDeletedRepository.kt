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

package studio.lunabee.onesafe.domain.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import java.time.Instant
import java.util.UUID

interface SafeItemDeletedRepository {
    suspend fun getDeletedItemsByDeletedParent(deletedParentId: UUID?, order: ItemOrder): List<SafeItem>
    suspend fun getSiblingOriginalChildren(parentId: UUID, order: ItemOrder): List<SafeItem>
    suspend fun updateSiblingOriginalChildrenParentId(parentId: UUID, newParentId: UUID?)
    fun countSafeItemByParentIdDeletedFlow(parentId: UUID?): Flow<Int>
    suspend fun countSafeItemByParentIdDeleted(parentId: UUID?): Int
    fun getPagerItemByParentIdDeleted(config: PagingConfig, parentId: UUID?, order: ItemOrder): Flow<PagingData<SafeItem>>
    fun countAllDeletedWithNonDeletedParent(): Flow<Int>
    suspend fun getHighestDeletedPosition(parentId: UUID?): Double?
    suspend fun removeItem(id: UUID)
    suspend fun removeItems(ids: List<UUID>)
    suspend fun findDeletedByIdWithDeletedDescendants(id: UUID): List<SafeItem>
    suspend fun findByIdWithDeletedAncestors(id: UUID): List<SafeItem>
    suspend fun restoreItemToParentWithDescendants(id: UUID?)
    suspend fun updateParentToNonDeletedAncestor(id: UUID)
    suspend fun removeOldItems(threshold: Instant)
    fun getAllDeletedItemsCount(): Flow<Int>
}
