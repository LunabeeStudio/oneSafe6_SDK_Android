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

import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class EncryptFieldsUseCase @Inject constructor(
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoRepository: MainCryptoRepository,
) {
    suspend operator fun invoke(
        itemId: UUID,
        fieldsData: List<ItemFieldData>,
    ): List<SafeItemField> {
        val key = safeItemKeyRepository.getSafeItemKey(itemId)
        return this(itemId, fieldsData, key)
    }

    suspend operator fun invoke(
        itemId: UUID,
        fieldsData: List<ItemFieldData>,
        key: SafeItemKey,
    ): List<SafeItemField> {
        val encryptEntries = mutableListOf<EncryptEntry<Any>?>()

        fieldsData.forEach { data ->
            encryptEntries += data.name?.let { EncryptEntry(it) } // +0
            encryptEntries += data.placeholder?.let { EncryptEntry(it) } // +1
            encryptEntries += data.value?.let { EncryptEntry(it) } // +2
            encryptEntries += data.kind?.let { EncryptEntry(it) } // +3
            encryptEntries += data.secureDisplayMask?.let { EncryptEntry(it) } // +4
            encryptEntries += data.formattingMask?.let { EncryptEntry(it) } // +5
        }
        val fieldEncryptedPropertiesCount = 6

        val encryptedEntries = cryptoRepository.encrypt(key, encryptEntries)
        return fieldsData.mapIndexed { idx, data ->
            val offsetIdx = idx * fieldEncryptedPropertiesCount
            SafeItemField(
                id = UUID.randomUUID(),
                encName = encryptedEntries[offsetIdx],
                position = data.position,
                itemId = itemId,
                encPlaceholder = encryptedEntries[offsetIdx + 1],
                encValue = encryptedEntries[offsetIdx + 2],
                showPrediction = data.showPrediction,
                encKind = encryptedEntries[offsetIdx + 3],
                updatedAt = Instant.now(),
                isItemIdentifier = data.isItemIdentifier,
                encSecureDisplayMask = encryptedEntries[offsetIdx + 4],
                encFormattingMask = encryptedEntries[offsetIdx + 5],
                isSecured = data.isSecured,
            )
        }
    }
}
