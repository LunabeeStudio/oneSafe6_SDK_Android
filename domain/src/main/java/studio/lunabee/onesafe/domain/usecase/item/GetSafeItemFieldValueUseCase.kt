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
 * Created by Lunabee Studio / Date - 5/17/2023 - for the oneSafe6 SDK.
 * Last modified 5/17/23, 4:23 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldValue
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.DecryptUseCase
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

/**
 * Get a single item value to be displayed in a specific screen, with the item color.
 */
class GetSafeItemFieldValueUseCase @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val safeItemRepository: SafeItemRepository,
    private val decryptUseCase: DecryptUseCase,
) {
    suspend operator fun invoke(itemId: UUID, fieldId: UUID): LBResult<SafeItemFieldValue> {
        return OSError.runCatching {
            val encColor = safeItemRepository.getSafeItem(id = itemId).encColor
            val safeItemField = safeItemFieldRepository.getSafeItemField(fieldId = fieldId)
            val encFieldValue = safeItemField.encValue
            val encFieldName = safeItemField.encName
            val encKind = safeItemField.encKind

            val decColor: String? = (
                encColor?.let {
                    decryptUseCase(encColor, itemId, String::class)
                } as? LBResult.Success
                )?.successData

            val decValue: String? = (
                encFieldValue?.let {
                    decryptUseCase(encFieldValue, itemId, String::class)
                } as? LBResult.Success
                )?.successData

            val decName: String? = (
                encFieldName?.let {
                    decryptUseCase(encFieldName, itemId, String::class)
                } as? LBResult.Success
                )?.successData

            val decKind: SafeItemFieldKind? = (
                encKind?.let {
                    decryptUseCase(encKind, itemId, SafeItemFieldKind::class)
                } as? LBResult.Success
                )?.successData

            SafeItemFieldValue(
                color = decColor,
                fieldValue = decValue,
                fieldName = decName,
                fieldKind = decKind,
            )
        }
    }
}
