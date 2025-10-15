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

package studio.lunabee.onesafe.domain.utils

import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.SetIconUseCase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

/**
 * Helper from creating a new [SafeItem] for the current loaded safe
 */
class SafeItemBuilder @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
    private val setIconUseCase: SetIconUseCase,
    private val safeRepository: SafeRepository,
) {
    suspend fun build(data: Data): Pair<SafeItemKey, SafeItem> {
        val itemKey = cryptoRepository.generateKeyForItemId(data.id)
        val safeId = safeRepository.currentSafeId()

        val iconId: UUID? = data.icon?.let {
            setIconUseCase(itemKey, data.icon, safeId)
        }

        val item = SafeItem(
            id = data.id,
            encName = data.name?.let { cryptoRepository.encrypt(itemKey, EncryptEntry(it)) },
            parentId = data.parentId,
            isFavorite = data.isFavorite,
            updatedAt = data.updatedAt,
            position = data.position,
            iconId = iconId,
            encColor = data.color?.let { cryptoRepository.encrypt(itemKey, EncryptEntry(it)) },
            deletedAt = null,
            deletedParentId = null,
            indexAlpha = data.indexAlpha,
            createdAt = data.createdAt,
            safeId = safeId,
        )

        return itemKey to item
    }

    data class Data(
        val name: String?,
        val parentId: UUID?,
        val isFavorite: Boolean,
        val icon: ByteArray?,
        val color: String?,
        val id: UUID,
        val position: Double,
        val updatedAt: Instant,
        val indexAlpha: Double,
        val createdAt: Instant,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Data

            if (name != other.name) return false
            if (parentId != other.parentId) return false
            if (isFavorite != other.isFavorite) return false
            if (icon != null) {
                if (other.icon == null) return false
                if (!icon.contentEquals(other.icon)) return false
            } else if (other.icon != null) {
                return false
            }
            if (color != other.color) return false
            if (id != other.id) return false
            if (position != other.position) return false
            if (updatedAt != other.updatedAt) return false
            if (indexAlpha != other.indexAlpha) return false
            if (createdAt != other.createdAt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name?.hashCode() ?: 0
            result = 31 * result + (parentId?.hashCode() ?: 0)
            result = 31 * result + isFavorite.hashCode()
            result = 31 * result + (icon?.contentHashCode() ?: 0)
            result = 31 * result + (color?.hashCode() ?: 0)
            result = 31 * result + id.hashCode()
            result = 31 * result + position.hashCode()
            result = 31 * result + updatedAt.hashCode()
            result = 31 * result + indexAlpha.hashCode()
            result = 31 * result + createdAt.hashCode()
            return result
        }
    }
}
