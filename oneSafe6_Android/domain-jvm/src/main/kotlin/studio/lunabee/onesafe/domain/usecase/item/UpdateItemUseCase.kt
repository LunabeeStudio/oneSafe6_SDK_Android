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
 * Created by Lunabee Studio / Date - 5/17/2023 - for the oneSafe6 SDK.
 * Last modified 5/17/23, 4:58 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.common.UpdateState
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.DeleteIconUseCase
import studio.lunabee.onesafe.domain.usecase.SetIconUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val setIconUseCase: SetIconUseCase,
    private val cryptoRepository: MainCryptoRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val indexWordEntryRepository: IndexWordEntryRepository,
    private val deleteIconUseCase: DeleteIconUseCase,
    private val addAndRemoveFileUseCase: AddAndRemoveFileUseCase,
    private val createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase,
    private val updateFieldsUseCase: UpdateFieldsUseCase,
    private val computeItemAlphaIndexUseCase: ComputeItemAlphaIndexUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        itemId: UUID,
        updateData: UpdateData,
        fields: List<ItemFieldData> = emptyList(),
        fileSavingData: List<FileSavingData> = emptyList(),
    ): LBResult<SafeItem> {
        return OSError.runCatching {
            // Get current safe item.
            val safeItem = safeItemRepository.getSafeItem(id = itemId)
            val itemKey = safeItemKeyRepository.getSafeItemKey(id = itemId)

            // Update or remove icon if needed.
            val iconId: UUID? = when (updateData.icon) {
                is UpdateState.ModifiedTo -> updateData.icon.newValue?.let { icon ->
                    setIconUseCase(
                        itemKey = itemKey,
                        icon = icon,
                        safeId = safeItem.safeId,
                    ).also {
                        safeItem.iconId?.let { currentIconId -> deleteIconUseCase(currentIconId) }
                    }
                }
                is UpdateState.Removed -> {
                    deleteIconUseCase(safeItem = safeItem)
                    null
                }
                is UpdateState.Unchanged -> safeItem.iconId
            }

            val (encName: ByteArray?, indexAlpha: Double) = when (updateData.name) {
                is UpdateState.ModifiedTo -> {
                    val newName = updateData.name.newValue
                    newName?.let {
                        cryptoRepository.encrypt(
                            itemKey,
                            EncryptEntry(newName),
                        )
                    } to computeItemAlphaIndexUseCase(newName, safeItem.indexAlpha).getOrThrow()
                }
                is UpdateState.Removed -> null to computeItemAlphaIndexUseCase(null, safeItem.indexAlpha).getOrThrow()
                is UpdateState.Unchanged -> safeItem.encName to safeItem.indexAlpha
            }

            val encColor: ByteArray? = when (updateData.color) {
                is UpdateState.ModifiedTo -> updateData.color.newValue?.let {
                    cryptoRepository.encrypt(
                        itemKey,
                        EncryptEntry(it),
                    )
                }
                is UpdateState.Removed -> null
                is UpdateState.Unchanged -> safeItem.encColor
            }

            var itemWithUpdatedValue = safeItem.copy(
                encName = encName,
                iconId = iconId,
                encColor = encColor,
                indexAlpha = indexAlpha,
            )

            val indexWordEntries = if (itemWithUpdatedValue != safeItem) {
                indexWordEntryRepository.deleteNameIndexFromItemId(itemId)
                when (val name = updateData.name) {
                    is UpdateState.Unchanged<String?> -> name.value
                    is UpdateState.ModifiedTo<String?> -> name.newValue
                    else -> null
                }?.let { itemName ->
                    createIndexWordEntriesFromItemUseCase(name = itemName, id = itemId)
                }
            } else {
                null
            }

            // Updated at
            itemWithUpdatedValue = itemWithUpdatedValue.copy(updatedAt = Instant.now(clock))

            safeItemRepository.updateSafeItem(
                safeItem = itemWithUpdatedValue,
                indexWordEntries = indexWordEntries,
            )

            // UpdateFields
            updateFieldsUseCase(safeItem, fields)

            // Add files from new fields
            // remove files from removed fields
            addAndRemoveFileUseCase(item = safeItem, fileSavingData = fileSavingData)

            itemWithUpdatedValue
        }
    }

    class UpdateData(
        val name: UpdateState<String?>,
        val icon: UpdateState<ByteArray?>,
        val color: UpdateState<String?>,
    )
}
