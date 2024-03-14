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

/**
 * This interface is used as a cache object in order to split import correctly.
 * ALL OBJECTS SHOULD BE RESET AS SOON AS POSSIBLE.
 */
interface ImportCacheDataSource {
    /**
     * Metadata exported and mapping from archive.
     */
    var importMetadata: ImportMetadata?

    /**
     * Master key generated with the password entered by user and the salt available in the archive.
     * This salt is in data file (this is not the "default" salt that we used for decrypting data file).
     * This master will be used to decrypt old data.
     */
    var archiveMasterKey: ByteArray?

    /**
     * Content decrypted from Protobuf data file. This is the full content of decrypted file.
     */
    var archiveContent: OSExportProto.Archive?

    // Following variables are used as dictionary for mapping old and news ids.
    val newItemIdsByOldOnes: MutableMap<UUID, UUID>
    val newIconIdsByOldOnes: MutableMap<UUID, UUID>
    val newFieldIdsByOldOnes: MutableMap<UUID, UUID>
    val newFileIdsByOldOnes: MutableMap<UUID, UUID>
    val thumbnails: MutableMap<UUID, ByteArray>
    val reEncryptedSafeItemKeys: MutableMap<UUID, SafeItemKey?>

    // Following variables contains the final object to save in database.
    // All object are now correctly re-encrypted with current user info and with brand new ids.
    val migratedSafeItemsToImport: MutableList<SafeItem>
    var migratedSafeItemFieldsToImport: List<SafeItemField>
    val migratedSearchIndexToImport: MutableList<IndexWordEntry>
    val allItemAlphaIndices: MutableMap<UUID, Double>
    var rootItemData: Pair<String, Double>?
    var migratedIconsToImport: List<File>
    var migratedFilesToImport: List<File>
    val newEncryptedValue: MutableMap<UUID, ByteArray>

    /**
     * Clean cache if an error occurred during authentication phase.
     * Metadata are not cleaned as they should be valid at this point.
     */
    fun cleanOnAuthError() {
        archiveContent = null
        archiveMasterKey = null
    }

    fun clearAll() {
        importMetadata = null
        archiveMasterKey = null
        archiveContent = null
        rootItemData = null
        newItemIdsByOldOnes.clear()
        newIconIdsByOldOnes.clear()
        newFieldIdsByOldOnes.clear()
        newFieldIdsByOldOnes.clear()
        reEncryptedSafeItemKeys.clear()
        migratedSafeItemsToImport.clear()
        migratedSafeItemFieldsToImport = emptyList()
        migratedSearchIndexToImport.clear()
        migratedIconsToImport = emptyList()
        migratedFilesToImport = emptyList()
        newEncryptedValue.clear()
        allItemAlphaIndices.clear()
    }
}
