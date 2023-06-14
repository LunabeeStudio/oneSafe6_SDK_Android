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

import com.google.protobuf.InvalidProtocolBufferException
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import com.lunabee.lblogger.i
import com.lunabee.lblogger.v
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.common.SortedAncestors
import studio.lunabee.onesafe.domain.engine.ImportEngine
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.PersistenceManager
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.proto.OSExportProto
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveSafeItem
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveSafeItemField
import studio.lunabee.onesafe.use
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import javax.inject.Inject

class ImportEngineImpl @Inject constructor(
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
    private val importCryptoRepository: ImportExportCryptoRepository,
    private val mainCryptoRepository: MainCryptoRepository,
    private val safeItemRepository: SafeItemRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val idProvider: ItemIdProvider,
    private val iconIdProvider: IconIdProvider,
    private val importCacheDataSource: ImportCacheDataSource,
    private val persistenceManager: PersistenceManager,
    private val createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase,
    private val createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase,
) : ImportEngine {
    override suspend fun getMetadata(archiveExtractedDirectory: File): ImportMetadata {
        // TODO Until we found a better solution, clear all cache if an new import is started.
        importCacheDataSource.clearAll()
        return withContext(fileDispatcher) {
            val metadataFile = File(archiveExtractedDirectory, ArchiveConstants.MetadataFile)
            if (metadataFile.exists()) {
                metadataFile.inputStream().use {
                    val protoMetadata = OSExportProto.ArchiveMetadata.parseFrom(it)
                    LOG.i(message = "Metadata extracted correctly from platform ${protoMetadata.fromPlatform}")
                    val importMetadata = ImportMetadata(
                        isFromOldOneSafe = protoMetadata.isFromOneSafePlus,
                        archiveKind = protoMetadata.archiveKind.toOSArchiveKind(),
                        itemCount = protoMetadata.itemsCount,
                        archiveVersion = protoMetadata.archiveVersion,
                        fromPlatform = protoMetadata.fromPlatform,
                        createdAt = Instant.parse(protoMetadata.createdAt),
                    )
                    importCacheDataSource.importMetadata = importMetadata
                    importMetadata
                }
            } else {
                importCacheDataSource.importMetadata = null
                archiveExtractedDirectory.deleteRecursively()
                throw OSImportExportError(code = OSImportExportError.Code.METADATA_FILE_NOT_FOUND)
            }
        }
    }

    override fun getMetadataFromCache(): LBResult<ImportMetadata> {
        return importCacheDataSource.importMetadata?.let {
            LBResult.Success(it)
        } ?: LBResult.Failure(OSImportExportError(code = OSImportExportError.Code.METADATA_NOT_IN_CACHE))
    }

    override fun authenticateAndExtractData(
        archiveExtractedDirectory: File,
        password: CharArray,
    ): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                val dataFile = File(archiveExtractedDirectory, ArchiveConstants.DataFile)
                if (!dataFile.exists()) {
                    LOG.e("Data file does not exist in the archive build from ${importCacheDataSource.importMetadata?.fromPlatform}")
                    emit(LBFlowResult.Failure(OSImportExportError(code = OSImportExportError.Code.DATA_FILE_NOT_FOUND)))
                    return@flow
                }

                val archiveContent = dataFile.inputStream().use {
                    OSExportProto.Archive.parseFrom(it)
                }
                importCacheDataSource.archiveContent = archiveContent
                importCacheDataSource.archiveMasterKey = password.use {
                    importCryptoRepository.deriveKey(
                        password = password,
                        salt = archiveContent.salt.toByteArrayOrNull() ?: throw OSImportExportError(OSImportExportError.Code.SALT_INVALID),
                    )
                }

                // Try to decrypt first key to check credentials
                archiveContent.keysList.firstOrNull()?.let { itemKey ->
                    importCryptoRepository.decryptRawItemKey(
                        cipherData = itemKey.value.toByteArray(),
                        key = importCacheDataSource.archiveMasterKey!!,
                    )
                }

                emit(LBFlowResult.Success(Unit))
            } catch (e: OSCryptoError) {
                finishWithError(OSImportExportError(code = OSImportExportError.Code.WRONG_CREDENTIALS, cause = e))
            } catch (e: OSImportExportError) {
                // Use a separate block to handle this type of error differently (i.e credential might be valid but archive is flaky)
                finishWithError(e)
            } catch (e: InvalidProtocolBufferException) {
                finishWithError(OSImportExportError(OSImportExportError.Code.ARCHIVE_MALFORMED))
            }
        }.onStart { emit(LBFlowResult.Loading()) }
    }

    private suspend fun FlowCollector<LBFlowResult<Unit>>.finishWithError(e: OSError) {
        importCacheDataSource.cleanOnAuthError()
        emit(LBFlowResult.Failure(e))
    }

    override fun prepareDataForImport(archiveExtractedDirectory: File): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                generateNewIdsForSafeItemsAndFields()
                mapAndReEncryptSafeItemKeyFromArchive()
                finishIdsMigration(archiveExtractedDirectory = archiveExtractedDirectory)
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                importCacheDataSource.newItemIdsByOldOnes.clear()
                importCacheDataSource.newIconIdsByOldOnes.clear()
                importCacheDataSource.newFieldIdsByOldOnes.clear()
                importCacheDataSource.reEncryptedSafeItemKeys.clear()
                importCacheDataSource.migratedIconsToImport = emptyList()
                importCacheDataSource.migratedSafeItemsToImport.clear()
                importCacheDataSource.migratedSafeItemFieldsToImport = emptyList()
                importCacheDataSource.migratedSearchIndexToImport.clear()
                emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.UNEXPECTED_ERROR, cause = e)))
            }
        }.onStart { emit(LBFlowResult.Loading()) }
    }

    override fun saveImportData(mode: ImportMode): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                when (mode) {
                    ImportMode.AppendInFolder -> {
                        val itemId: UUID = idProvider()
                        val importParentItemKey = mainCryptoRepository.generateKeyForItemId(itemId)
                        val importParentItem = SafeItem(
                            id = itemId,
                            encName = mainCryptoRepository.encrypt(importParentItemKey, EncryptEntry(buildAppendItemName())),
                            parentId = null,
                            isFavorite = false,
                            updatedAt = Instant.now(),
                            position = 0.0,
                            iconId = null,
                            encColor = null,
                            deletedAt = null,
                            deletedParentId = null,
                        )

                        importCacheDataSource.migratedSafeItemsToImport.replaceAll { item ->
                            if (item.parentId == null) {
                                item.copy(parentId = importParentItem.id)
                            } else {
                                item
                            }
                        }
                        importCacheDataSource.migratedSafeItemsToImport += importParentItem
                        importCacheDataSource.reEncryptedSafeItemKeys += importParentItemKey.id to importParentItemKey
                    }
                    ImportMode.Replace -> {
                        persistenceManager.clearAll()
                    }

                    ImportMode.Append -> {
                        /* No op */
                        // No need to clear everything or to create a folder as above
                    }
                }

                // We iterate over all the icons urls to rename them using the new icons ids.
                importCacheDataSource.migratedIconsToImport.map { icon ->
                    val oldIconId = icon.nameWithoutExtension.let(UUID::fromString)
                    importCacheDataSource.newIconIdsByOldOnes[oldIconId]?.let { newIconId ->
                        iconRepository.copyAndDeleteIconFile(iconFile = icon, iconId = newIconId)
                    }
                }

                val safeItems =
                    SortedAncestors(safeItemsNotSorted = importCacheDataSource.migratedSafeItemsToImport).sortByAncestors()

                safeItemRepository.save(
                    safeItems,
                    importCacheDataSource.reEncryptedSafeItemKeys.values.filterNotNull(),
                    importCacheDataSource.migratedSafeItemFieldsToImport,
                    importCacheDataSource.migratedSearchIndexToImport,
                )
                importCacheDataSource.clearAll()
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                // Nothing to clean in failure state as retry is possible.
                emit(LBFlowResult.Failure(e))
            }
        }.onStart { emit(LBFlowResult.Loading()) }
    }

    private suspend fun generateNewIdsForSafeItemsAndFields() {
        val itemList = importCacheDataSource.archiveContent?.itemsList.orEmpty()
        val existingItemsIds: List<UUID> = safeItemRepository.getAllSafeItemIds()
        itemList.associateTo(importCacheDataSource.newItemIdsByOldOnes) { archiveSafeItem ->
            val oldItemId: UUID = UUID.fromString(archiveSafeItem.id)
            var newItemId: UUID = idProvider()
            while (existingItemsIds.contains(newItemId)) newItemId = idProvider()
            oldItemId to newItemId
        }

        val itemFieldList = importCacheDataSource.archiveContent?.fieldsList.orEmpty()
        val existingItemFieldIds: List<UUID> = safeItemFieldRepository.getAllSafeItemFieldIds()
        itemFieldList.associateTo(importCacheDataSource.newFieldIdsByOldOnes) { archiveSafeItemField ->
            val oldItemFieldId: UUID = UUID.fromString(archiveSafeItemField.id)
            var newItemFieldId: UUID = idProvider()
            while (existingItemFieldIds.contains(newItemFieldId)) newItemFieldId = idProvider()
            oldItemFieldId to newItemFieldId
        }
    }

    /**
     * Decrypt safe item key with credentials generated during archive creation process and re-encrypt it with current credentials.
     * Use the decrypted safe item key to create search index
     * @return a map indexing by old id, with the new [SafeItemKey] re-encrypted with a brand new id
     */
    private suspend fun mapAndReEncryptSafeItemKeyFromArchive() {
        importCacheDataSource
            .archiveContent
            ?.keysList
            .orEmpty()
            .associateTo(importCacheDataSource.reEncryptedSafeItemKeys) { archiveSafeItemKey ->
                val oldId = archiveSafeItemKey.id.let(UUID::fromString)
                val oldRawItemKeyValue: ByteArray = importCryptoRepository.decryptRawItemKey(
                    cipherData = archiveSafeItemKey.value.toByteArray(),
                    key = importCacheDataSource.archiveMasterKey!!,
                )

                createSearchIndexWithDecryptedSafeItemKey(archiveSafeItemKey.id, oldRawItemKeyValue)

                val safeItemKey = importCacheDataSource.newItemIdsByOldOnes[oldId]?.let { newId ->
                    mainCryptoRepository.importItemKey(oldRawItemKeyValue, newId)
                }

                if (safeItemKey == null) {
                    LOG.e(message = "Key as no associated SafeItem. Export archive is corrupted.")
                    LOG.v(message = "Key $oldId as no associated SafeItem. See above log for platform details.")
                }
                oldId to safeItemKey
            }
    }

    /**
     * Take a safeItemKey before re-encryption and create the search for the all the items and fields concerned by the key.
     * Store the index in the [importCacheDataSource]
     */
    private suspend fun createSearchIndexWithDecryptedSafeItemKey(safeItemId: String, oldRawItemKeyValue: ByteArray) {
        val oldItemId = safeItemId.let(UUID::fromString)
        val newItemId = importCacheDataSource.newItemIdsByOldOnes[oldItemId]!!

        // Search index for item
        val item = importCacheDataSource.archiveContent!!.itemsList.first { it.id == safeItemId }
        val plainName = importCryptoRepository.decrypt(
            cipherData = item.encName.toByteArray(),
            key = oldRawItemKeyValue,
        ).decodeToString()
        val itemIndexEntries = createIndexWordEntriesFromItemUseCase(name = plainName, id = newItemId)
        importCacheDataSource.migratedSearchIndexToImport += itemIndexEntries

        // Search index for fields
        val archiveSafeItemFields = importCacheDataSource.archiveContent!!.fieldsList.filter { it.itemId == safeItemId }
        val itemFieldsDataToIndex = archiveSafeItemFields
            .mapNotNull { archiveSafeItemField ->
                val oldFieldIdKey = archiveSafeItemField.id.let(UUID::fromString)
                val newFieldId = importCacheDataSource.newFieldIdsByOldOnes[oldFieldIdKey]!!
                // Filter empty value
                if (!archiveSafeItemField.encValue.isEmpty) {
                    val plainValue = importCryptoRepository.decrypt(
                        cipherData = archiveSafeItemField.encValue.toByteArray(),
                        key = oldRawItemKeyValue,
                    ).decodeToString()
                    ItemFieldDataToIndex(
                        value = plainValue,
                        isSecured = archiveSafeItemField.isSecured,
                        itemId = newItemId,
                        fieldId = newFieldId,
                    )
                } else {
                    null
                }
            }
        val fieldIndexEntries = createIndexWordEntriesFromItemFieldUseCase(data = itemFieldsDataToIndex)
        importCacheDataSource.migratedSearchIndexToImport += fieldIndexEntries
    }

    // TODO make sure every icon file has an item, and every item with icon has an icon file
    private fun finishIdsMigration(archiveExtractedDirectory: File) {
        val archiveContent = importCacheDataSource.archiveContent!!
        val newSafeItemIdsByOldOnes = importCacheDataSource.newItemIdsByOldOnes
        val newSafeItemFieldIdsByOldOnes = importCacheDataSource.newFieldIdsByOldOnes

        val safeItemsToImport = mapSafeItemsFromArchive(archiveSafeItems = archiveContent.itemsList)
        val safeItemFieldsToImport = mapSafeItemFieldsFromArchive(archiveSafeFieldItems = archiveContent.fieldsList)
        importCacheDataSource.migratedIconsToImport = File(archiveExtractedDirectory, ArchiveConstants.IconFolder).listFiles()
            ?.toList()
            .orEmpty()
        // First, we gather all the already used ids.
        val existingIconsIds: List<String> = iconRepository.getIcons().map { it.nameWithoutExtension }

        // We iterate over all the items.
        safeItemsToImport.mapTo(importCacheDataSource.migratedSafeItemsToImport) { item ->
            val oldItemId: UUID = item.id
            val migratedItem = item.copy(
                id = newSafeItemIdsByOldOnes.getValue(key = oldItemId),
                parentId = item.parentId?.let(newSafeItemIdsByOldOnes::getValue),
                deletedParentId = item.deletedParentId?.let(newSafeItemIdsByOldOnes::getValue),
            )

            // Re-generate and map icon ids to avoid collision with existing icons (double import)
            item.iconId?.let { oldIconId ->
                // Keep new icon id as UUID to let Room handle mapping later.
                var newIconId: UUID = iconIdProvider()
                // We create the new icon id.
                while (existingIconsIds.contains(newIconId.toString())) newIconId = iconIdProvider()
                // We store the old and new ids into the mapping dictionary.
                importCacheDataSource.newIconIdsByOldOnes[oldIconId] = newIconId
                migratedItem.copy(iconId = newIconId)
            } ?: migratedItem
        }

        importCacheDataSource.migratedSafeItemFieldsToImport = safeItemFieldsToImport.mapNotNull { field ->
            val oldItemFieldId: UUID = field.id
            val newItemFieldId = newSafeItemFieldIdsByOldOnes.getValue(key = oldItemFieldId)
            newSafeItemIdsByOldOnes[field.itemId]?.let { itemId ->
                field.copy(id = newItemFieldId, itemId = itemId)
            }.apply {
                if (this == null) {
                    LOG.e("Field as no associated SafeItem")
                }
            }
        }
    }

    private fun mapSafeItemsFromArchive(archiveSafeItems: List<ArchiveSafeItem>): List<SafeItem> {
        return archiveSafeItems.map { archiveSafeItem ->
            SafeItem(
                id = UUID.fromString(archiveSafeItem.id),
                encName = archiveSafeItem.encName.toByteArrayOrNull(),
                parentId = archiveSafeItem.parentId.nullIfEmpty()?.let(UUID::fromString),
                isFavorite = archiveSafeItem.isFavorite,
                updatedAt = Instant.parse(archiveSafeItem.updatedAt),
                position = archiveSafeItem.position,
                iconId = archiveSafeItem.iconId.nullIfEmpty()?.let(UUID::fromString),
                encColor = archiveSafeItem.encColor.toByteArrayOrNull(),
                deletedAt = archiveSafeItem.deletedAt.nullIfEmpty()?.let { Instant.parse(it) }?.takeIf {
                    archiveSafeItem.deletedParentId.nullIfEmpty() != null
                },
                deletedParentId = archiveSafeItem.deletedParentId.nullIfEmpty()?.let(UUID::fromString)?.takeIf {
                    archiveSafeItem.deletedAt.nullIfEmpty() != null
                },
            )
        }
    }

    private fun mapSafeItemFieldsFromArchive(archiveSafeFieldItems: List<ArchiveSafeItemField>): List<SafeItemField> {
        return archiveSafeFieldItems.map { archiveSafeItemField ->
            SafeItemField(
                id = UUID.fromString(archiveSafeItemField.id),
                encName = archiveSafeItemField.encName.toByteArrayOrNull(),
                position = archiveSafeItemField.position,
                itemId = UUID.fromString(archiveSafeItemField.itemId),
                encPlaceholder = archiveSafeItemField.encPlaceholder.toByteArrayOrNull(),
                encValue = archiveSafeItemField.encValue.toByteArrayOrNull(),
                showPrediction = archiveSafeItemField.showPrediction,
                encKind = archiveSafeItemField.encKind.toByteArrayOrNull(),
                updatedAt = Instant.parse(archiveSafeItemField.updatedAt),
                isItemIdentifier = archiveSafeItemField.isItemIdentifier,
                encFormattingMask = archiveSafeItemField.formattingMask.toByteArrayOrNull(),
                encSecureDisplayMask = archiveSafeItemField.secureDisplayMask.toByteArrayOrNull(),
                isSecured = archiveSafeItemField.isSecured,
            )
        }
    }

    companion object {
        private val LOG = LBLogger.get("<Import>")
        private const val DefaultParentItem: String = "Import"

        private fun buildAppendItemName(): String {
            return "$DefaultParentItem " +
                "${DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(LocalDate.now())} " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }
}
