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
 * Last modified 12/12/23, 10:35 AM
 */

package studio.lunabee.onesafe.importexport.repository

import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemIdName
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import java.util.UUID

interface ImportExportItemRepository {
    suspend fun save(
        items: List<SafeItem>,
        safeItemKeys: List<SafeItemKey>,
        fields: List<SafeItemField>,
        indexWordEntries: MutableList<IndexWordEntry>,
        updateItemsAlphaIndices: MutableMap<UUID, Double>,
    )

    suspend fun getAllSafeItemIds(safeId: SafeId): List<UUID>

    suspend fun getAllSafeItemIdName(safeId: SafeId): List<SafeItemIdName>
}
