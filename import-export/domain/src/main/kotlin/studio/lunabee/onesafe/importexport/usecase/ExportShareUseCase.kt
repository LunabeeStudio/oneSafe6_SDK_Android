/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 2:17 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.importexport.engine.ShareExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.importexport.model.ExportItem
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import java.io.File
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

class ExportShareUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val iconRepository: IconRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val fileRepository: FileRepository,
    private val archiveZipUseCase: ArchiveZipUseCase,
    private val mainCryptoRepository: MainCryptoRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        exportEngine: ShareExportEngine,
        itemToShare: UUID,
        includeChildren: Boolean,
        archiveExtractedDirectory: File,
    ): Flow<LBFlowResult<File>> {
        return flow {
            // Get all item and re-encrypt keys with export key
            val items = safeItemRepository.getSafeItemsAndChildren(itemToShare, includeChildren)
            val safeItemsWithKeys = items.associate { safeItem ->
                ExportItem(safeItem, keepFavorite = false) to
                    safeItemKeyRepository.getSafeItemKey(id = safeItem.id).also { itemKey ->
                        mainCryptoRepository.reEncryptItemKey(itemKey, exportEngine.exportKey)
                    }
            }
            val itemsId: List<UUID> = safeItemsWithKeys.keys.map { it.id }
            val iconsId: List<String> = safeItemsWithKeys.keys.map { it.iconId.toString() }
            val safeItemFields = safeItemFieldRepository.getAllSafeItemFieldsOfItems(itemsId)

            val fileIdList = safeItemFields.mapNotNull { field ->
                val kind = field.encKind?.let { kind -> decryptUseCase(kind, field.itemId, String::class).data }
                kind?.takeIf { SafeItemFieldKind.isKindFile(SafeItemFieldKind.fromString(kind)) }?.let {
                    field.encValue?.let { encValue ->
                        decryptUseCase(encValue, field.itemId, String::class).data?.substringBefore(Constant.FileTypeExtSeparator)
                    }
                }
            }

            val data = ExportData(
                safeItemsWithKeys = safeItemsWithKeys,
                safeItemFields = safeItemFields,
                icons = iconRepository.getIcons(iconsId),
                files = fileRepository.getFiles(fileIdList),
            )

            emitAll(
                exportEngine.createExportArchiveContent(
                    dataHolderFolder = archiveExtractedDirectory,
                    data = data,
                    archiveKind = OSArchiveKind.Sharing,
                    safeId = safeRepository.currentSafeId(),
                ).flatMapLatest { result ->
                    when (result) {
                        is LBFlowResult.Failure -> flowOf(LBFlowResult.Failure(throwable = result.throwable))
                        is LBFlowResult.Loading -> flowOf(LBFlowResult.Loading(progress = result.progress))
                        // Zip if creation is success. Will return the progress of zip.
                        is LBFlowResult.Success -> archiveZipUseCase(
                            folderToZip = archiveExtractedDirectory,
                            outputZipFile = File(archiveExtractedDirectory, buildArchiveName(clock)),
                        )
                    }
                },
            )
        }
    }

    companion object {
        /**
         * Return final archive name (i.e oneSafe-20221212-134622.os6lss)
         */
        private fun buildArchiveName(clock: Clock): String {
            return listOf(
                ImportExportConstant.ArchiveFilePrefix,
                ImportExportConstant.ArchiveDateFormatter.format(LocalDate.now(clock)), // i.e 20230113
                ImportExportConstant.ArchiveTimeFormatter.format(LocalTime.now(clock)), // i.e 134602
            ).joinToString(ImportExportConstant.ArchiveFileSeparator) + ".${ImportExportConstant.ExtensionOs6Sharing}"
        }
    }
}
