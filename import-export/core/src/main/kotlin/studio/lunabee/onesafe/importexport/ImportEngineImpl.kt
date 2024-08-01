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
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.common.SortedAncestors
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.SortItemNameUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.getOrThrow
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import studio.lunabee.onesafe.importexport.repository.ImportExportCryptoRepository
import studio.lunabee.onesafe.importexport.repository.ImportExportItemRepository
import studio.lunabee.onesafe.proto.OSExportProto
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveSafeItem
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveSafeItemField
import studio.lunabee.onesafe.toByteArray
import studio.lunabee.onesafe.use
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val log = LBLogger.get("<Import>")

@Singleton
class ImportEngineImpl @Inject constructor(
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
    private val importCryptoRepository: ImportExportCryptoRepository,
    private val mainCryptoRepository: MainCryptoRepository,
    private val itemRepository: ImportExportItemRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val fileRepository: FileRepository,
    private val idProvider: ItemIdProvider,
    private val iconIdProvider: IconIdProvider,
    private val fileIdProvider: FileIdProvider,
    private val importCacheDataSource: ImportCacheDataSource,
    private val createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase,
    private val createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase,
    private val moveToBinItemUseCase: MoveToBinItemUseCase,
    private val itemDecryptUseCase: ItemDecryptUseCase,
    private val sortItemNameUseCase: SortItemNameUseCase,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) : ImportEngine {
    override suspend fun getMetadata(archiveExtractedDirectory: File): ImportMetadata {
        // TODO Until we found a better solution, clear all cache if an new import is started.
        importCacheDataSource.clearAll()
        return withContext(fileDispatcher) {
            val metadataFile = File(archiveExtractedDirectory, ArchiveConstants.MetadataFile)
            if (metadataFile.exists()) {
                metadataFile.inputStream().use {
                    val protoMetadata = OSExportProto.ArchiveMetadata.parseFrom(it)
                    log.i(message = "Metadata extracted correctly from platform ${protoMetadata.fromPlatform}")
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
                    log.e("Data file does not exist in the archive build from ${importCacheDataSource.importMetadata?.fromPlatform}")
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

    override fun prepareDataForImport(archiveExtractedDirectory: File, mode: ImportMode): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                val safeId = safeRepository.currentSafeId()
                generateNewIdsForSafeItemsAndFields(safeId)
                generateNewIdsForFiles()
                mapAndReEncryptSafeItemKeyFromArchive(mode = mode, safeId = safeId)
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
                importCacheDataSource.allItemAlphaIndices.clear()
                importCacheDataSource.rootItemData = null
                emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.UNEXPECTED_ERROR, cause = e)))
            }
        }.onStart { emit(LBFlowResult.Loading()) }
    }

    override fun saveImportData(mode: ImportMode): Flow<LBFlowResult<Unit>> {
        return flow {
            val safeId = safeRepository.currentSafeId()
            try {
                when (mode) {
                    ImportMode.AppendInFolder -> {
                        val itemId: UUID = idProvider()
                        val importParentItemKey = mainCryptoRepository.generateKeyForItemId(itemId)
                        val rootItemData = importCacheDataSource.rootItemData!!
                        val now = Instant.now(clock)
                        val importParentItem = SafeItem(
                            id = itemId,
                            encName = mainCryptoRepository.encrypt(importParentItemKey, EncryptEntry(rootItemData.first)),
                            parentId = null,
                            isFavorite = false,
                            updatedAt = now,
                            position = 0.0,
                            iconId = null,
                            encColor = null,
                            deletedAt = null,
                            deletedParentId = null,
                            indexAlpha = rootItemData.second,
                            createdAt = now,
                            safeId = safeId,
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
                        moveToBinItemUseCase.all()
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
                        iconRepository.copyAndDeleteIconFile(iconFile = icon, iconId = newIconId, safeId = safeId)
                    }
                }

                // We iterate over all the file urls to rename them using the new file ids.
                importCacheDataSource.migratedFilesToImport.map { file ->
                    val oldFileId = file.nameWithoutExtension.let(UUID::fromString)
                    importCacheDataSource.newFileIdsByOldOnes[oldFileId]?.let { newFileId ->
                        fileRepository.copyAndDeleteFile(file = file, fileId = newFileId, safeId = safeId)
                    }
                }

                val safeItems =
                    SortedAncestors(safeItemsNotSorted = importCacheDataSource.migratedSafeItemsToImport).sortByAncestors()

                itemRepository.save(
                    items = safeItems,
                    safeItemKeys = importCacheDataSource.reEncryptedSafeItemKeys.values.filterNotNull(),
                    fields = importCacheDataSource.migratedSafeItemFieldsToImport,
                    indexWordEntries = importCacheDataSource.migratedSearchIndexToImport,
                    updateItemsAlphaIndices = importCacheDataSource.allItemAlphaIndices,
                )
                importCacheDataSource.clearAll()
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                // Nothing to clean in failure state as retry is possible.
                emit(LBFlowResult.Failure(e))
            }
        }.onStart { emit(LBFlowResult.Loading()) }
    }

    private suspend fun generateNewIdsForSafeItemsAndFields(safeId: SafeId) {
        val itemList = importCacheDataSource.archiveContent?.itemsList.orEmpty()
        val existingItemsIds: List<UUID> = itemRepository.getAllSafeItemIds(safeId)
        itemList.associateTo(importCacheDataSource.newItemIdsByOldOnes) { archiveSafeItem ->
            val oldItemId: UUID = UUID.fromString(archiveSafeItem.id)
            var newItemId: UUID = idProvider()
            while (existingItemsIds.contains(newItemId)) newItemId = idProvider()
            oldItemId to newItemId
        }

        val itemFieldList = importCacheDataSource.archiveContent?.fieldsList.orEmpty()
        val existingItemFieldIds: List<UUID> = safeItemFieldRepository.getAllSafeItemFieldIds(safeId)
        itemFieldList.associateTo(importCacheDataSource.newFieldIdsByOldOnes) { archiveSafeItemField ->
            val oldItemFieldId: UUID = UUID.fromString(archiveSafeItemField.id)
            var newItemFieldId: UUID = idProvider()
            while (existingItemFieldIds.contains(newItemFieldId)) newItemFieldId = idProvider()
            oldItemFieldId to newItemFieldId
        }
    }

    private suspend fun generateNewIdsForFiles() {
        val itemFieldList = importCacheDataSource.archiveContent?.fieldsList.orEmpty()
        itemFieldList.forEach { archiveSafeItemField ->
            val encKey = importCacheDataSource.archiveContent?.keysList?.firstOrNull { it.id == archiveSafeItemField.itemId }
                ?: return@forEach
            val decryptedKey: ByteArray = importCryptoRepository.decryptRawItemKey(
                cipherData = encKey.value.toByteArray(),
                key = importCacheDataSource.archiveMasterKey!!,
            )
            val itemFieldKind = SafeItemFieldKind.fromString(
                importCryptoRepository.decrypt(
                    cipherData = archiveSafeItemField.encKind.toByteArray(),
                    key = decryptedKey,
                ).decodeToString(),
            )
            val fieldId = UUID.fromString(archiveSafeItemField.id)
            importCacheDataSource.thumbnails[fieldId] = importCryptoRepository.encrypt(
                Constant.ThumbnailPlaceHolderName.toByteArray(),
                decryptedKey,
            )
            if (SafeItemFieldKind.isKindFile(itemFieldKind)) {
                val decryptedValue = importCryptoRepository.decrypt(
                    cipherData = archiveSafeItemField.encValue.toByteArray(),
                    key = decryptedKey,
                ).decodeToString()
                val fileId = decryptedValue.substringBefore(Constant.FileTypeExtSeparator)
                val newId = fileIdProvider()
                val newValue = decryptedValue.replace(fileId, newId.toString())
                val newEncValue = importCryptoRepository.encrypt(newValue.encodeToByteArray(), decryptedKey)
                importCacheDataSource.newEncryptedValue[fieldId] = newEncValue
                importCacheDataSource.newFileIdsByOldOnes[UUID.fromString(fileId)] = newId
            }
        }
    }

    /**
     * Decrypt safe item key with credentials generated during archive creation process and re-encrypt it with current credentials.
     *   - Use the plain safe item key to create search index
     *   - Use the plain safe item name to compute the alphabetic index
     */
    private suspend fun mapAndReEncryptSafeItemKeyFromArchive(mode: ImportMode, safeId: SafeId) {
        val currentPlainItemsName = itemRepository.getAllSafeItemIdName(safeId)
            .associate { itemIdName ->
                itemIdName.id to itemIdName.encName?.let {
                    itemDecryptUseCase(it, itemIdName.id, String::class).getOrThrow()
                }.orEmpty()
            }

        val plainItemsName = mutableListOf<Pair<UUID, String>>()
        currentPlainItemsName.mapTo(plainItemsName) { (id, name) -> id to name }

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

                createSearchIndexWithDecryptedSafeItemKey(archiveSafeItemKey.id, oldRawItemKeyValue, plainItemsName)

                val safeItemKey = importCacheDataSource.newItemIdsByOldOnes[oldId]?.let { newId ->
                    mainCryptoRepository.importItemKey(oldRawItemKeyValue, newId)
                }

                if (safeItemKey == null) {
                    log.e(message = "Key as no associated SafeItem. Export archive is corrupted.")
                    log.v(message = "Key $oldId as no associated SafeItem. See above log for platform details.")
                }
                oldId to safeItemKey
            }

        val appendItemSortId = UUID(0L, 0L)
        val appendItemName = buildAppendItemName()
        if (mode == ImportMode.AppendInFolder) {
            plainItemsName.add(appendItemSortId to appendItemName)
        }

        sortItemNameUseCase(plainItemsName).forEach { (id, index) ->
            if (id == appendItemSortId) {
                importCacheDataSource.rootItemData = appendItemName to index
            } else {
                importCacheDataSource.allItemAlphaIndices += id to index
            }
        }
    }

    /**
     * Take a safeItemKey before re-encryption and create the search for the all the items and fields concerned by the key.
     * Store the index in the [importCacheDataSource]
     */
    private suspend fun createSearchIndexWithDecryptedSafeItemKey(
        safeItemId: String,
        oldRawItemKeyValue: ByteArray,
        plainItemNames: MutableList<Pair<UUID, String>>,
    ) {
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

        // Fill currentPlainItemNames with new items
        plainItemNames += newItemId to plainName

        // Ignore media and files fields for search index
        val fieldsToIgnore = importCacheDataSource.newEncryptedValue.keys
        // Search index for fields
        val archiveSafeItemFields = importCacheDataSource.archiveContent!!.fieldsList.filter {
            it.itemId == safeItemId && !fieldsToIgnore.contains(UUID.fromString(it.id))
        }
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
    private suspend fun finishIdsMigration(archiveExtractedDirectory: File) {
        val archiveContent = importCacheDataSource.archiveContent!!
        val newSafeItemIdsByOldOnes = importCacheDataSource.newItemIdsByOldOnes
        val newSafeItemFieldIdsByOldOnes = importCacheDataSource.newFieldIdsByOldOnes

        val safeItemsToImport = mapSafeItemsFromArchive(archiveSafeItems = archiveContent.itemsList)
        val safeItemFieldsToImport = mapSafeItemFieldsFromArchive(archiveSafeFieldItems = archiveContent.fieldsList)
        importCacheDataSource.migratedIconsToImport = File(archiveExtractedDirectory, ArchiveConstants.IconFolder).listFiles()
            ?.toList()
            .orEmpty()
        importCacheDataSource.migratedFilesToImport = File(archiveExtractedDirectory, ArchiveConstants.FileFolder).listFiles()
            ?.toList()
            .orEmpty()
        // First, we gather all the already used ids.
        @OptIn(CrossSafeData::class)
        val existingIconsIds: List<String> = iconRepository.getAllIcons().map { it.nameWithoutExtension }
        val migratedIconIdsToImport = importCacheDataSource.migratedIconsToImport.map { it.nameWithoutExtension }

        // We iterate over all the items.
        safeItemsToImport.mapTo(importCacheDataSource.migratedSafeItemsToImport) { item ->
            // Re-generate and map icon ids to avoid collision with existing icons (double import)
            item.iconId?.takeIf { migratedIconIdsToImport.contains(it.toString()) }?.let { oldIconId ->
                // Keep new icon id as UUID to let Room handle mapping later.
                var newIconId: UUID = iconIdProvider()
                // We create the new icon id.
                while (existingIconsIds.contains(newIconId.toString())) newIconId = iconIdProvider()
                // We store the old and new ids into the mapping dictionary.
                importCacheDataSource.newIconIdsByOldOnes[oldIconId] = newIconId
                item.copy(iconId = newIconId)
            } ?: item.copy(iconId = null)
        }

        importCacheDataSource.migratedSafeItemFieldsToImport = safeItemFieldsToImport.mapNotNull { field ->
            val oldItemFieldId: UUID = field.id
            val newItemFieldId = newSafeItemFieldIdsByOldOnes.getValue(key = oldItemFieldId)
            val thumbnail = importCacheDataSource.thumbnails[oldItemFieldId]
            val newItemFieldEncryptedValue = importCacheDataSource.newEncryptedValue[oldItemFieldId] ?: field.encValue
            newSafeItemIdsByOldOnes[field.itemId]?.let { itemId ->
                field.copy(
                    id = newItemFieldId,
                    itemId = itemId,
                    encValue = newItemFieldEncryptedValue,
                    encThumbnailFileName = thumbnail,
                )
            }.apply {
                if (this == null) {
                    log.e("Field as no associated SafeItem")
                }
            }
        }
    }

    private suspend fun mapSafeItemsFromArchive(archiveSafeItems: List<ArchiveSafeItem>): List<SafeItem> {
        val newSafeItemIdsByOldOnes = importCacheDataSource.newItemIdsByOldOnes
        val safeId = safeRepository.currentSafeId()
        return archiveSafeItems.map { archiveSafeItem ->
            val oldItemId: UUID = UUID.fromString(archiveSafeItem.id)
            val newItemId = newSafeItemIdsByOldOnes[oldItemId]!!

            SafeItem(
                id = newItemId,
                encName = archiveSafeItem.encName.toByteArrayOrNull(),
                parentId = archiveSafeItem.parentId.nullIfEmpty()?.let(UUID::fromString)?.let(newSafeItemIdsByOldOnes::get),
                isFavorite = archiveSafeItem.isFavorite,
                updatedAt = Instant.parse(archiveSafeItem.updatedAt),
                position = archiveSafeItem.position,
                iconId = archiveSafeItem.iconId.nullIfEmpty()?.let(UUID::fromString),
                encColor = archiveSafeItem.encColor.toByteArrayOrNull(),
                deletedAt = archiveSafeItem.deletedAt.nullIfEmpty()?.let { Instant.parse(it) },
                deletedParentId = if (archiveSafeItem.deletedAt.nullIfEmpty() != null) {
                    archiveSafeItem.deletedParentId.nullIfEmpty()?.let {
                        newSafeItemIdsByOldOnes[UUID.fromString(it)]
                    }
                } else {
                    null
                },
                indexAlpha = importCacheDataSource.allItemAlphaIndices[newItemId]!!,
                createdAt = Instant.parse(archiveSafeItem.createdAt),
                safeId = safeId,
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
                encThumbnailFileName = null,
            )
        }
    }

    private fun buildAppendItemName(): String {
        val localDateTime = LocalDateTime.now(clock)
        return "$DefaultParentItem " +
            "${DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(localDateTime)} " +
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    companion object {
        private const val DefaultParentItem: String = "Import"
    }
}
