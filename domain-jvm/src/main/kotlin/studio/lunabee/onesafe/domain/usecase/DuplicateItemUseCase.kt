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

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.DuplicatedNameTransform
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.ComputeItemAlphaIndexUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.utils.SafeItemBuilder
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.getOrThrow
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

private val log = LBLogger.get<DuplicateItemUseCase>()

/**
 * Get, duplicate and persist a [SafeItem]
 */
class DuplicateItemUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val safeItemRepository: SafeItemRepository,
    private val createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase,
    private val createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val duplicateNameTransform: DuplicatedNameTransform,
    private val itemIdProvider: ItemIdProvider,
    private val fileIdProvider: FileIdProvider,
    private val safeItemBuilder: SafeItemBuilder,
    private val deleteIconUseCase: DeleteIconUseCase,
    private val encryptFieldsUseCase: EncryptFieldsUseCase,
    private val fileRepository: FileRepository,
    private val fieldIdProvider: FieldIdProvider,
    private val computeItemAlphaIndexUseCase: ComputeItemAlphaIndexUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        itemId: UUID,
    ): LBResult<SafeItem> {
        return OSError.runCatching(log, { OSDomainError(OSDomainError.Code.DUPLICATE_ICON_FAILED, cause = it.cause) }) {
            val allItems = safeItemRepository.findByIdWithChildren(itemId)
            val mappedIds = allItems.associate { it.id to itemIdProvider() } // generate new id for every duplicates

            val originalItem = allItems.first()

            val nextSiblingPosition = safeItemRepository.getNextSiblingPosition(itemId)
            val duplicatedItemPosition = nextSiblingPosition?.let {
                (originalItem.position + it) / 2
            } ?: (originalItem.position + 1.0)

            val firstDuplicationData = duplicateItem(
                originalItem = originalItem,
                duplicatedId = mappedIds[originalItem.id]!!,
                duplicatedParentId = originalItem.parentId,
                transformName = duplicateNameTransform,
                position = duplicatedItemPosition,
            )

            val allChildren = allItems.subList(1, allItems.size)

            val duplicationDataList = mutableListOf(firstDuplicationData)

            allChildren.forEach { item ->
                val duplicationData = duplicateItem(
                    originalItem = item,
                    duplicatedId = mappedIds[item.id]!!,
                    duplicatedParentId = item.parentId?.let { mappedIds[it] },
                )
                duplicationDataList += duplicationData
            }

            val safeItemKeys = duplicationDataList.map { it.key }
            val duplicatedFields = encryptToField(duplicationDataList)
            val safeItems = duplicationDataList.map { it.item }
            try {
                val indexWordEntries =
                    createIndexWordEntriesFromDuplicatedItems(duplicationDataList) +
                        createIndexWordEntriesFromDuplicatedFields(duplicationDataList, duplicatedFields)
                safeItemRepository.save(safeItems, safeItemKeys, duplicatedFields, indexWordEntries)
            } catch (e: OSStorageError) {
                duplicationDataList.forEach { data ->
                    if (data.item.iconId != null) {
                        deleteIconUseCase(data.item)
                    }
                }
                throw e
            }

            duplicationDataList.first().item
        }
    }

    private suspend fun duplicateItem(
        originalItem: SafeItem,
        duplicatedId: UUID,
        duplicatedParentId: UUID?,
        transformName: DuplicatedNameTransform? = null,
        position: Double? = null,
    ): DuplicationData {
        val originalKey = safeItemKeyRepository.getSafeItemKey(originalItem.id)

        val originalCryptoEntries: MutableList<DecryptEntry<out Any>?> = mutableListOf(
            originalItem.encName?.let { DecryptEntry(it, String::class) },
            originalItem.encColor?.let { DecryptEntry(it, String::class) },
        )

        val originalFields = safeItemFieldRepository.getSafeItemFields(originalItem.id)
        originalFields.forEach { field ->
            originalCryptoEntries += field.encName?.let { DecryptEntry(it, String::class) } // +0
            originalCryptoEntries += field.encKind?.let { DecryptEntry(it, SafeItemFieldKind::class) } // +1
            originalCryptoEntries += field.encPlaceholder?.let { DecryptEntry(it, String::class) } // +2
            originalCryptoEntries += field.encValue?.let { DecryptEntry(it, String::class) } // +3
            originalCryptoEntries += field.encFormattingMask?.let { DecryptEntry(it, String::class) } // +4
            originalCryptoEntries += field.encSecureDisplayMask?.let { DecryptEntry(it, String::class) } // +5
        }
        val fieldEncryptedPropertiesCount = 6

        val plainEntries = cryptoRepository.decrypt(originalKey, originalCryptoEntries)

        val name = plainEntries[0] as String? // -1
        val color = plainEntries[1] as String? // -2
        val itemEncryptedPropertiesCount = 2

        val icon = originalItem.iconId?.let { getIconUseCase(it, originalItem.id) }?.let { iconResult ->
            when (iconResult) {
                is LBResult.Failure -> {
                    // Log the error but do not set the duplication as failed
                    log.e(OSDomainError(OSDomainError.Code.DUPLICATE_ICON_FAILED, cause = iconResult.throwable))
                    null
                }
                is LBResult.Success -> iconResult.successData
            }
        }
        val transformedName = transformName?.invoke(name) ?: name
        val indexAlpha = computeItemAlphaIndexUseCase(transformedName).getOrThrow("Failed to compute item alpha index")
        val now = Instant.now(clock)
        val (duplicatedItemKey, duplicatedItem) = safeItemBuilder.build(
            SafeItemBuilder.Data(
                name = transformedName,
                parentId = duplicatedParentId,
                isFavorite = false,
                icon = icon,
                color = color,
                id = duplicatedId,
                position = position ?: originalItem.position,
                updatedAt = now,
                indexAlpha = indexAlpha,
                createdAt = now,
            ),
        )

        val itemFieldsData = mutableListOf<ItemFieldData>()
        val fieldEntries = plainEntries.subList(itemEncryptedPropertiesCount, plainEntries.size)
        originalFields.forEachIndexed { idx, originalField ->
            val offsetIdx = idx * fieldEncryptedPropertiesCount
            itemFieldsData += ItemFieldData(
                id = fieldIdProvider(),
                name = fieldEntries[offsetIdx] as String?,
                position = originalField.position,
                placeholder = fieldEntries[offsetIdx + 2] as String?,
                value = fieldEntries[offsetIdx + 3] as String?,
                kind = (fieldEntries[offsetIdx + 1] as SafeItemFieldKind?),
                showPrediction = originalField.showPrediction,
                isItemIdentifier = originalField.isItemIdentifier,
                formattingMask = fieldEntries[offsetIdx + 4] as String?,
                secureDisplayMask = fieldEntries[offsetIdx + 5] as String?,
                isSecured = originalField.isSecured,
            )
        }

        return DuplicationData(
            key = duplicatedItemKey,
            item = duplicatedItem,
            plainName = transformedName,
            fields = itemFieldsData,
            originalKey = originalKey,
        )
    }

    private suspend fun encryptToField(
        data: List<DuplicationData>,
    ): List<SafeItemField> {
        return data.flatMap { entry ->
            val itemFieldsData = entry.fields
            val key = entry.key
            // Copy file fields
            val finalItemFieldsData = itemFieldsData.mapNotNull { data ->
                if (data.kind?.let(SafeItemFieldKind::isKindFile) != true) {
                    data
                } else {
                    var newFileId = fileIdProvider()
                    while (fileRepository.getFile(newFileId.toString()).exists()) {
                        newFileId = fileIdProvider()
                    }
                    val originalFileId = data.value?.substringBefore(Constant.FileTypeExtSeparator) ?: return@mapNotNull null
                    val originalFile = fileRepository.getFile(originalFileId)
                    val tempFile = fileRepository.createTempFile(newFileId.toString())
                    val inputStream = cryptoRepository.getDecryptStream(originalFile, entry.originalKey)
                    val outputStream = cryptoRepository.getEncryptStream(tempFile, key)
                    try {
                        inputStream.use {
                            outputStream.use {
                                inputStream.copyTo(outputStream)
                            }
                        }
                        fileRepository.copyAndDeleteFile(tempFile, newFileId, entry.item.safeId)
                    } catch (e: Throwable) {
                        // If error, remove file, don't save the field
                        fileRepository.deleteFile(newFileId)
                        return@mapNotNull null
                    }
                    val newValue = "$newFileId${Constant.FileTypeExtSeparator}${data.value.substringAfter(Constant.FileTypeExtSeparator)}"
                    data.copy(value = newValue)
                }
            }

            val itemId = entry.item.id
            encryptFieldsUseCase(itemId, finalItemFieldsData, key)
        }
    }

    private suspend fun createIndexWordEntriesFromDuplicatedItems(duplicationDataList: List<DuplicationData>): List<IndexWordEntry> {
        return duplicationDataList.mapNotNull { duplicationData ->
            duplicationData.plainName?.let { name ->
                createIndexWordEntriesFromItemUseCase(name, duplicationData.item.id)
            }
        }.flatten()
    }

    private suspend fun createIndexWordEntriesFromDuplicatedFields(
        duplicationDataList: List<DuplicationData>,
        duplicatedFields: List<SafeItemField>,
    ): List<IndexWordEntry> {
        val fieldsForIndex = duplicationDataList.flatMap { it.fields }
            .zip(duplicatedFields)
            .mapNotNull { (plain, encrypt) ->
                plain.value?.let {
                    ItemFieldDataToIndex(
                        plain.value,
                        plain.isSecured,
                        encrypt.itemId,
                        encrypt.id,
                    )
                }
            }
        return createIndexWordEntriesFromItemFieldUseCase(fieldsForIndex)
    }

    private class DuplicationData(
        val key: SafeItemKey,
        val originalKey: SafeItemKey,
        val item: SafeItem,
        val plainName: String?,
        val fields: List<ItemFieldData>,
    )
}
