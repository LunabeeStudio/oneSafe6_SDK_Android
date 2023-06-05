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

import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.kotlin.toByteString
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import studio.lunabee.onesafe.domain.engine.ExportEngine
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.qualifier.DateFormatterType
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.proto.OSExportProto
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveMetadata.ArchiveKind
import studio.lunabee.onesafe.proto.archive
import studio.lunabee.onesafe.proto.archiveMetadata
import studio.lunabee.onesafe.proto.archiveSafeItem
import studio.lunabee.onesafe.proto.archiveSafeItemField
import studio.lunabee.onesafe.proto.archiveSafeItemKey
import studio.lunabee.onesafe.randomize
import studio.lunabee.onesafe.use
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportEngineImpl @Inject constructor(
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
    private val importExportCryptoRepository: ImportExportCryptoRepository,
    @DateFormatterType(type = DateFormatterType.Type.IsoInstant) private val dateTimeFormatter: DateTimeFormatter,
    private val exportCacheDataSource: ExportCacheDataSource,
) : ExportEngine {
    override val exportKey: ByteArray
        get() = exportCacheDataSource.archiveMasterKey!!

    override fun prepareSharing(password: CharArray, platformInfo: String): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                importExportCryptoRepository.createMasterKeyAndSalt(password).let {
                    exportCacheDataSource.archiveMasterKey = it.first
                    exportCacheDataSource.exportSalt = it.second
                }
                exportCacheDataSource.fromPlatformVersion = platformInfo
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                clearCache()
                emit(LBFlowResult.Failure(e))
            }
        }
    }

    override fun prepareBackup(password: CharArray, platformInfo: String, masterSalt: ByteArray): Flow<LBFlowResult<Unit>> {
        return flow {
            try {
                exportCacheDataSource.archiveMasterKey = importExportCryptoRepository.deriveKey(password, masterSalt)
                exportCacheDataSource.fromPlatformVersion = platformInfo
                exportCacheDataSource.exportSalt = masterSalt
                emit(LBFlowResult.Success(Unit))
            } catch (e: Exception) {
                clearCache()
                emit(LBFlowResult.Failure(e))
            }
        }
    }

    private fun clearCache() {
        exportCacheDataSource.archiveMasterKey?.randomize()
        exportCacheDataSource.archiveMasterKey = null
        exportCacheDataSource.fromPlatformVersion = null
        exportCacheDataSource.exportSalt = null
    }

    override fun createExportArchiveContent(
        dataHolderFolder: File,
        safeItemsWithKeys: Map<SafeItem, SafeItemKey>,
        safeItemFields: List<SafeItemField>,
        icons: List<File>,
        archiveKind: OSArchiveKind,
    ): Flow<LBFlowResult<Unit>> {
        return flow {
            dataHolderFolder.mkdirs(override = true)
            createArchiveMetadataFile(
                folderDestination = dataHolderFolder,
                platformInfo = exportCacheDataSource.fromPlatformVersion!!, // fail if not set
                itemCount = safeItemsWithKeys.size,
                archiveKind = archiveKind.toProtoArchiveKind(),
            )
            createArchiveData(folderDestination = dataHolderFolder, safeItemsWithKeys = safeItemsWithKeys, safeItemFields = safeItemFields)
            copyIconFilesToExportFolder(icons = icons, folderDestination = dataHolderFolder)
            emit(LBFlowResult.Success(Unit))
        }.flowOn(fileDispatcher)
    }

    /**
     * Create metadata file that will be wrapped into the final archive.
     * This file is not encrypted. It contains data for UI during import.
     */
    private suspend fun FlowCollector<LBFlowResult<Unit>>.createArchiveMetadataFile(
        folderDestination: File,
        platformInfo: String,
        itemCount: Int,
        archiveKind: ArchiveKind,
    ) {
        emit(LBFlowResult.Loading(progress = 1f)) // TODO add constants to indicate current step in process
        try {
            val archiveMetadata =
                createArchiveMetadata(safeItemCount = itemCount, platformInfo = platformInfo, kind = archiveKind)
            val metadataDestFile = File(folderDestination, Constants.MetadataFile)
            archiveMetadata.writeAsFile(destFile = metadataDestFile)
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_METADATA_FAILURE, cause = e)))
        }
    }

    /**
     * Create data file that will be wrapped into the final archive.
     */
    private suspend fun FlowCollector<LBFlowResult<Unit>>.createArchiveData(
        folderDestination: File,
        safeItemsWithKeys: Map<SafeItem, SafeItemKey>,
        safeItemFields: List<SafeItemField>,
    ) {
        emit(LBFlowResult.Loading(progress = 2f)) // TODO add constants to indicate current step in process
        try {
            val safeItemKeysToExport: List<OSExportProto.ArchiveSafeItemKey> =
                createArchiveSafeItemKeys(safeItemsWithKeys.values.toList())
            val safeItemFieldsToExport: List<OSExportProto.ArchiveSafeItemField> =
                createArchiveSafeItemFields(safeItemFields)
            val safeItemsToExport: List<OSExportProto.ArchiveSafeItem> =
                createArchiveSafeItems(safeItemsWithKeys.keys.toList())
            val archiveData: OSExportProto.Archive = archive {
                salt = exportCacheDataSource.exportSalt!!.use(ByteArray::byteStringOrEmpty)
                items.addAll(safeItemsToExport)
                fields.addAll(safeItemFieldsToExport)
                keys.addAll(safeItemKeysToExport)
            }
            File(folderDestination, Constants.DataFile).outputStream().use { outputStream ->
                archiveData.writeTo(outputStream)
            }
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_DATA_FAILURE, cause = e)))
        }
    }

    /**
     * Take all the icons files from current installation and copy it into the export folder.
     */
    private suspend fun FlowCollector<LBFlowResult<Unit>>.copyIconFilesToExportFolder(icons: List<File>, folderDestination: File) {
        emit(LBFlowResult.Loading(progress = 3f)) // TODO add constants to indicate current step in process
        try {
            val iconFolder = File(folderDestination, Constants.IconFolder)
            if (!iconFolder.exists()) iconFolder.mkdirs()
            icons.forEach { file ->
                file.copyTo(target = File(iconFolder, file.name))
            }
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_ICON_FAILURE, cause = e)))
        }
    }

    private fun createArchiveSafeItemKeys(safeItemKeys: List<SafeItemKey>): List<OSExportProto.ArchiveSafeItemKey> {
        return safeItemKeys.map { safeItemKey ->
            archiveSafeItemKey {
                id = safeItemKey.id.toString()
                value = safeItemKey.encValue.toByteString()
            }
        }
    }

    private fun createArchiveSafeItemFields(safeItemFields: List<SafeItemField>): List<OSExportProto.ArchiveSafeItemField> {
        return safeItemFields.map { safeItemField ->
            archiveSafeItemField {
                id = safeItemField.id.toString()
                createdAt = dateTimeFormatter.format(safeItemField.updatedAt.atZone(ZoneId.systemDefault()))
                itemId = safeItemField.itemId.toString()
                encKind = safeItemField.encKind.byteStringOrEmpty()
                encName = safeItemField.encName.byteStringOrEmpty()
                encPlaceholder = safeItemField.encPlaceholder.byteStringOrEmpty()
                encValue = safeItemField.encValue.byteStringOrEmpty()
                position = safeItemField.position
                showPrediction = safeItemField.showPrediction
                updatedAt = dateTimeFormatter.format(safeItemField.updatedAt.atZone(ZoneId.systemDefault())) // TODO create date in field,
                isItemIdentifier = safeItemField.isItemIdentifier
                formattingMask = safeItemField.encFormattingMask.byteStringOrEmpty()
                secureDisplayMask = safeItemField.encSecureDisplayMask.byteStringOrEmpty()
                isSecured = safeItemField.isSecured
            }
        }
    }

    private fun createArchiveMetadata(
        safeItemCount: Int,
        platformInfo: String,
        kind: ArchiveKind,
    ): OSExportProto.ArchiveMetadata {
        return archiveMetadata {
            archiveVersion = com.lunabee.onesafe.proto.Constants.ArchiveSpecVersion
            archiveKind = kind
            fromPlatform = platformInfo
            isFromOneSafePlus = false
            itemsCount = safeItemCount
            createdAt = dateTimeFormatter.format(Instant.now().atZone(ZoneId.systemDefault()))
        }
    }

    private fun createArchiveSafeItems(safeItems: List<SafeItem>): List<OSExportProto.ArchiveSafeItem> {
        return safeItems.map { safeItem ->
            archiveSafeItem {
                id = safeItem.id.toString()
                createdAt = dateTimeFormatter.format(Instant.now().atZone(ZoneId.systemDefault())) // TODO should be created
                deletedAt = safeItem.deletedAt?.let { dateTimeFormatter.format(it) }.orEmpty()
                deletedParentId = safeItem.deletedParentId?.toString().orEmpty()
                encColor = safeItem.encColor.byteStringOrEmpty()
                iconId = safeItem.iconId?.toString().orEmpty()
                encName = safeItem.encName.byteStringOrEmpty()
                parentId = safeItem.parentId?.toString().orEmpty()
                isFavorite = safeItem.isFavorite
                updatedAt = dateTimeFormatter.format(safeItem.updatedAt.atZone(ZoneId.systemDefault()))
                position = safeItem.position
            }
        }
    }

    /**
     * Private extension method to write a Protobuf object to a file.
     */
    private fun <B, P : GeneratedMessageLite<P, B>> P.writeAsFile(destFile: File) {
        destFile.createNewFile()
        destFile.outputStream().use { outputStream -> writeTo(outputStream) }
    }
}
