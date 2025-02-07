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
 * Created by Lunabee Studio / Date - 10/6/2023 - for the oneSafe6 SDK.
 * Last modified 06/10/2023 15:40
 */

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import java.util.UUID
import javax.inject.Inject

class DeleteFileAssociatedWithItemsUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val keyRepository: SafeItemKeyRepository,
    private val fileRepository: FileRepository,
) {

    suspend operator fun invoke(
        itemIds: List<UUID>,
    ) {
        val keys = keyRepository.getSafeItemKeys(itemIds)
        val fields = safeItemFieldRepository.getAllSafeItemFieldsOfItems(itemIds)
        val fieldMap = fields.groupBy { it.itemId }
        val keyToFieldMap: Map<SafeItemKey, List<SafeItemField>> = keys.associateWith { key ->
            fieldMap[key.id] ?: emptyList()
        }

        // For each item (itemKey), decrypt each fields kind in association with the field encValue and then decrypt value for file kind
        keyToFieldMap.forEach { (itemKey, fields) ->
            // Decrypt all fields kind at once. Ignore null values or kinds
            val encValueKindPairs: List<Pair<SafeItemField, String?>> = cryptoRepository.decryptWithData(
                itemKey,
                fields.mapNotNull { field ->
                    field.encKind?.let { encKind ->
                        field to DecryptEntry(encKind, String::class)
                    }
                },
            )

            // Iterate over value/kind pairs to delete
            encValueKindPairs.forEach kinds@{ (field, strKind) ->
                val kind = strKind?.let(SafeItemFieldKind::fromString) ?: return@kinds
                if (SafeItemFieldKind.isKindFile(kind)) {
                    field.encValue?.let {
                        val plainValue = cryptoRepository.decrypt(itemKey, DecryptEntry(it, String::class))
                        val fileId = plainValue.substringBefore(Constant.FileTypeExtSeparator)
                        fileRepository.deleteFile(fileId = UUID.fromString(fileId))
                    }
                    field.encThumbnailFileName?.let {
                        val thumbnailFileName = cryptoRepository.decrypt(itemKey, DecryptEntry(it, UUID::class)).toString()
                        fileRepository.getThumbnailFile(thumbnailFileName, isFullWidth = true).delete()
                        fileRepository.getThumbnailFile(thumbnailFileName, isFullWidth = false).delete()
                    }
                }
            }
        }
    }
}
