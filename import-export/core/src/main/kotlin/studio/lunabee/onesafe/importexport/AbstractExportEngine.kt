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
import studio.lunabee.onesafe.domain.model.importexport.ExportProgress
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.engine.ExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportInfo
import studio.lunabee.onesafe.proto.OSExportProto
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveMetadata.ArchiveKind
import studio.lunabee.onesafe.proto.archive
import studio.lunabee.onesafe.proto.archiveMetadata
import studio.lunabee.onesafe.proto.archiveSafeItem
import studio.lunabee.onesafe.proto.archiveSafeItemField
import studio.lunabee.onesafe.proto.archiveSafeItemKey
import studio.lunabee.onesafe.use
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Core implementation of [ExportEngine] shared with export and share features
 */
abstract class AbstractExportEngine(
    private val fileDispatcher: CoroutineDispatcher,
    private val dateTimeFormatter: DateTimeFormatter,
) : ExportEngine {

    protected fun createExportArchiveContent(
        dataHolderFolder: File,
        data: ExportData,
        archiveKind: OSArchiveKind,
        exportInfo: ExportInfo,
    ): Flow<LBFlowResult<Unit>> {
        return flow {
            dataHolderFolder.mkdirs(override = true)
            createArchiveMetadataFile(
                folderDestination = dataHolderFolder,
                platformInfo = exportInfo.fromPlatformVersion,
                itemCount = data.safeItemsWithKeys.size,
                archiveKind = archiveKind.toProtoArchiveKind(),
            )
            createArchiveData(
                folderDestination = dataHolderFolder,
                safeItemsWithKeys = data.safeItemsWithKeys,
                safeItemFields = data.safeItemFields,
                exportInfo = exportInfo,
            )
            copyIconFilesToExportFolder(icons = data.icons, folderDestination = dataHolderFolder)
            copyFilesToExportFolder(files = data.files, folderDestination = dataHolderFolder)
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
        emit(LBFlowResult.Loading(progress = ExportProgress.BuildMetadata.value()))
        try {
            val archiveMetadata =
                createArchiveMetadata(safeItemCount = itemCount, platformInfo = platformInfo, kind = archiveKind)
            val metadataDestFile = File(folderDestination, ArchiveConstants.MetadataFile)
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
        exportInfo: ExportInfo,
    ) {
        emit(LBFlowResult.Loading(progress = ExportProgress.BuildData.value()))
        try {
            val safeItemKeysToExport: List<OSExportProto.ArchiveSafeItemKey> =
                createArchiveSafeItemKeys(safeItemsWithKeys.values.toList())
            val safeItemFieldsToExport: List<OSExportProto.ArchiveSafeItemField> =
                createArchiveSafeItemFields(safeItemFields)
            val safeItemsToExport: List<OSExportProto.ArchiveSafeItem> =
                createArchiveSafeItems(safeItemsWithKeys.keys.toList())
            val archiveData: OSExportProto.Archive = archive {
                salt = exportInfo.exportSalt.use(ByteArray::byteStringOrEmpty)
                items.addAll(safeItemsToExport)
                fields.addAll(safeItemFieldsToExport)
                keys.addAll(safeItemKeysToExport)
            }
            File(folderDestination, ArchiveConstants.DataFile).outputStream().use { outputStream ->
                archiveData.writeTo(outputStream)
            }
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_DATA_FAILURE, cause = e)))
        }
    }

    /**
     * Take all the icons files from current installation and copy it into the export icon folder.
     */
    private suspend fun FlowCollector<LBFlowResult<Unit>>.copyIconFilesToExportFolder(icons: List<File>, folderDestination: File) {
        emit(LBFlowResult.Loading(progress = ExportProgress.CopyIcons.value()))
        try {
            val iconFolder = File(folderDestination, ArchiveConstants.IconFolder)
            if (!iconFolder.exists()) iconFolder.mkdirs()
            icons.forEach { file ->
                file.copyTo(target = File(iconFolder, file.name))
            }
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_ICON_FAILURE, cause = e)))
        }
    }

    /**
     * Take all the files from current installation and copy it into the export file folder.
     */
    private suspend fun FlowCollector<LBFlowResult<Unit>>.copyFilesToExportFolder(files: List<File>, folderDestination: File) {
        emit(LBFlowResult.Loading(progress = ExportProgress.CopyIcons.value()))
        try {
            val fileFolder = File(folderDestination, ArchiveConstants.FileFolder)
            if (!fileFolder.exists()) fileFolder.mkdirs()
            files.forEach { file ->
                file.copyTo(target = File(fileFolder, file.name))
            }
        } catch (e: Exception) {
            emit(LBFlowResult.Failure(throwable = OSImportExportError(code = OSImportExportError.Code.EXPORT_FILE_FAILURE, cause = e)))
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
            archiveVersion = com.lunabee.onesafe.proto.ArchiveProtoConstants.ArchiveSpecVersion
            archiveKind = kind
            fromPlatform = platformInfo
            isFromOneSafePlus = false
            itemsCount = safeItemCount
            createdAt = dateTimeFormatter.format(ZonedDateTime.now())
        }
    }

    private fun createArchiveSafeItems(safeItems: List<SafeItem>): List<OSExportProto.ArchiveSafeItem> {
        return safeItems.map { safeItem ->
            archiveSafeItem {
                id = safeItem.id.toString()
                createdAt = dateTimeFormatter.format(ZonedDateTime.now()) // TODO should be created
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
