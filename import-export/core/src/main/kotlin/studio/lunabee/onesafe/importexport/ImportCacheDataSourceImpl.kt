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

package studio.lunabee.onesafe.importexport

import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.proto.OSExportProto
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportCacheDataSourceImpl @Inject constructor() : ImportCacheDataSource {
    override var importMetadata: ImportMetadata? = null
    override var archiveMasterKey: ByteArray? = null

    override var archiveContent: OSExportProto.Archive? = null

    override var newEncryptedValue: MutableMap<UUID, ByteArray> = mutableMapOf()
    override val newFileIdsByOldOnes: MutableMap<UUID, UUID> = mutableMapOf()
    override val thumbnails: MutableMap<UUID, ByteArray> = mutableMapOf()
    override var newItemIdsByOldOnes: MutableMap<UUID, UUID> = mutableMapOf()
    override var newIconIdsByOldOnes: MutableMap<UUID, UUID> = mutableMapOf()
    override var newFieldIdsByOldOnes: MutableMap<UUID, UUID> = mutableMapOf()

    override var reEncryptedSafeItemKeys: MutableMap<UUID, SafeItemKey?> = mutableMapOf()
    override var migratedSafeItemsToImport: MutableList<SafeItem> = mutableListOf()
    override var migratedSafeItemFieldsToImport: List<SafeItemField> = emptyList()
    override var migratedSearchIndexToImport: MutableList<IndexWordEntry> = mutableListOf()
    override var allItemAlphaIndices: MutableMap<UUID, Double> = mutableMapOf()
    override var rootItemData: Pair<String, Double>? = null
    override var migratedIconsToImport: List<File> = emptyList()
    override var migratedFilesToImport: List<File> = emptyList()
}
