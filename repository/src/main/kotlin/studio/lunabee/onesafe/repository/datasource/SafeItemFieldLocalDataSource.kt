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

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import java.util.UUID

interface SafeItemFieldLocalDataSource {
    suspend fun getSafeItemField(fieldId: UUID): SafeItemField
    suspend fun getSafeItemFields(itemId: UUID): List<SafeItemField>
    suspend fun save(safeItemField: SafeItemField, indexWordEntries: List<IndexWordEntry>)
    suspend fun save(safeItemFields: List<SafeItemField>, indexWordEntries: List<IndexWordEntry>)
    fun getSafeItemFieldsFlow(itemId: UUID): Flow<List<SafeItemField>>
    suspend fun deleteByItemId(itemId: UUID)
    suspend fun getAllSafeItemFieldIds(safeId: SafeId): List<UUID>
    suspend fun getAllSafeItemFields(safeId: SafeId): List<SafeItemField>
    suspend fun saveThumbnailFileName(fieldId: UUID, encThumbnailFileName: ByteArray?)
    suspend fun getAllSafeItemFieldsOfItems(items: List<UUID>): List<SafeItemField>
}
