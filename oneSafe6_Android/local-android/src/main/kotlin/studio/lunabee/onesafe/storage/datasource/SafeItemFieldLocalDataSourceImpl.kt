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

package studio.lunabee.onesafe.storage.datasource

import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.repository.datasource.SafeItemFieldLocalDataSource
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import studio.lunabee.onesafe.storage.utils.runSQL
import java.util.UUID
import javax.inject.Inject

class SafeItemFieldLocalDataSourceImpl @Inject constructor(
    private val safeItemFieldDao: SafeItemFieldDao,
    private val searchIndexDao: IndexWordEntryDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : SafeItemFieldLocalDataSource {
    override suspend fun getSafeItemField(fieldId: UUID): SafeItemField {
        return safeItemFieldDao.getSafeItemField(fieldId).toSafeItemField()
    }

    override suspend fun getSafeItemFields(itemId: UUID): List<SafeItemField> {
        return safeItemFieldDao.getSafeItemFields(itemId).map(RoomSafeItemField::toSafeItemField)
    }

    override suspend fun save(safeItemField: SafeItemField, indexWordEntries: List<IndexWordEntry>) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemFieldDao.insert(RoomSafeItemField.fromSafeItemField(safeItemField))
                searchIndexDao.insert(indexWordEntries.map(RoomIndexWordEntry::fromIndexWordEntry))
            }
        }
    }

    override suspend fun save(safeItemFields: List<SafeItemField>, indexWordEntries: List<IndexWordEntry>) {
        runSQL {
            transactionProvider.runAsTransaction {
                safeItemFieldDao.insert(safeItemFields.map(RoomSafeItemField::fromSafeItemField))
                searchIndexDao.insert(indexWordEntries.map(RoomIndexWordEntry::fromIndexWordEntry))
            }
        }
    }

    override fun getSafeItemFieldsFlow(itemId: UUID): Flow<List<SafeItemField>> {
        return safeItemFieldDao.getSafeItemFieldsAsFlow(itemId)
            .distinctUntilChanged()
            .mapValues(RoomSafeItemField::toSafeItemField)
    }

    override suspend fun deleteByItemId(itemId: UUID): Unit = safeItemFieldDao.deleteByItemId(itemId)

    override suspend fun getAllSafeItemFieldIds(safeId: SafeId): List<UUID> {
        return safeItemFieldDao.getAllSafeItemFieldIds(safeId)
    }

    override suspend fun getAllSafeItemFields(safeId: SafeId): List<SafeItemField> {
        return safeItemFieldDao.getAllSafeItemFields(safeId).map { it.toSafeItemField() }
    }

    override suspend fun saveThumbnailFileName(fieldId: UUID, encThumbnailFileName: ByteArray?) {
        return safeItemFieldDao.saveThumbnailFileName(fieldId, encThumbnailFileName)
    }

    override suspend fun getAllSafeItemFieldsOfItems(items: List<UUID>): List<SafeItemField> {
        return safeItemFieldDao.getAllSafeItemFieldsOfItems(items).map { it.toSafeItemField() }
    }
}
