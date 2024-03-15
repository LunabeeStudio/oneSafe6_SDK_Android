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

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.repository.datasource.SafeItemFieldLocalDataSource
import java.util.UUID
import javax.inject.Inject

class SafeItemFieldRepositoryImpl @Inject constructor(
    private val localDataSource: SafeItemFieldLocalDataSource,
) : SafeItemFieldRepository {
    override suspend fun getSafeItemField(fieldId: UUID): SafeItemField = localDataSource.getSafeItemField(fieldId)
    override suspend fun getSafeItemFields(itemId: UUID): List<SafeItemField> = localDataSource.getSafeItemFields(
        itemId,
    )

    override fun getSafeItemFieldsFlow(itemId: UUID): Flow<List<SafeItemField>> = localDataSource.getSafeItemFieldsFlow(
        itemId,
    )

    override suspend fun save(safeItemField: SafeItemField, indexWordEntries: List<IndexWordEntry>) {
        localDataSource.save(safeItemField, indexWordEntries)
    }

    override suspend fun save(safeItemFields: List<SafeItemField>, indexWordEntries: List<IndexWordEntry>): Unit =
        localDataSource.save(safeItemFields, indexWordEntries)

    override suspend fun saveThumbnailFileName(fieldId: UUID, encThumbnailFileName: ByteArray?) {
        localDataSource.saveThumbnailFileName(fieldId, encThumbnailFileName)
    }

    override suspend fun deleteByItemId(itemId: UUID): Unit = localDataSource.deleteByItemId(itemId)
    override suspend fun getAllSafeItemFieldIds(): List<UUID> {
        return localDataSource.getAllSafeItemFieldIds()
    }

    override suspend fun getAllSafeItemFields(): List<SafeItemField> {
        return localDataSource.getAllSafeItemFields()
    }

    override suspend fun getAllSafeItemFieldsOfItems(items: List<UUID>): List<SafeItemField> {
        return localDataSource.getAllSafeItemFieldsOfItems(items)
    }
}
