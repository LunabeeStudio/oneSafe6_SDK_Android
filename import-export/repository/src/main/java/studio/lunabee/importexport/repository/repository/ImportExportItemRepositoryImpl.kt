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
 * Created by Lunabee Studio / Date - 12/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/12/23, 10:36 AM
 */

package studio.lunabee.importexport.repository.repository

import studio.lunabee.importexport.repository.datasource.ImportExportSafeItemLocalDataSource
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.importexport.repository.ImportExportItemRepository
import java.util.UUID
import javax.inject.Inject

class ImportExportItemRepositoryImpl @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val localDataSource: ImportExportSafeItemLocalDataSource,
) : ImportExportItemRepository {
    override suspend fun getAllSafeItemIdName(): List<SafeItemIdName> {
        return safeItemRepository.getAllSafeItemIdName()
    }

    override suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>,
        indexWordEntries: MutableList<IndexWordEntry>,
        updateItemsAlphaIndices: MutableMap<UUID, Double>,
    ) {
        localDataSource.save(
            items,
            safeItemKeys,
            fields,
            indexWordEntries,
            updateItemsAlphaIndices,
        )
    }

    override suspend fun getAllSafeItemIds(): List<UUID> {
        return localDataSource.getAllSafeItemIds()
    }
}
