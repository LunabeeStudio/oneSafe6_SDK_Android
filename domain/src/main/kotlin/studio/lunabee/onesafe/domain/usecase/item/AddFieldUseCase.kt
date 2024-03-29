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
 * Last modified 5/5/23, 3:33 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.usecase.EncryptFieldsUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

/**
 * Create and persist a [SafeItemField]
 */
class AddFieldUseCase @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val encryptFieldsUseCase: EncryptFieldsUseCase,
    private val createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        itemId: UUID,
        itemFieldData: ItemFieldData,
    ): LBResult<SafeItemField> {
        val result = invoke(itemId, listOf(itemFieldData))
        return when (result) {
            is LBResult.Failure -> LBResult.Failure(result.throwable, result.failureData?.first())
            is LBResult.Success -> LBResult.Success(result.successData.first())
        }
    }

    suspend operator fun invoke(
        itemId: UUID,
        itemFieldsData: List<ItemFieldData>,
    ): LBResult<List<SafeItemField>> = withContext(dispatcher) {
        OSError.runCatching {
            val safeItemFields = encryptFieldsUseCase(itemId, itemFieldsData)
            val dataToIndex = itemFieldsData.zip(safeItemFields).mapNotNull { (data, safeField) ->
                data.value?.let {
                    ItemFieldDataToIndex(
                        value = data.value,
                        isSecured = data.isSecured,
                        itemId = safeField.itemId,
                        fieldId = safeField.id,
                    )
                }
            }
            val indexWordEntries = createIndexWordEntriesFromItemFieldUseCase(dataToIndex)
            safeItemFieldRepository.save(safeItemFields, indexWordEntries)
            safeItemFields
        }
    }
}
