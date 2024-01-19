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

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.utils.SafeItemBuilder
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.getOrThrow
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.math.floor

/**
 * Create and persist a [SafeItem]
 */
class CreateItemUseCase @Inject constructor(
    private val safeItemBuilder: SafeItemBuilder,
    private val safeItemRepository: SafeItemRepository,
    private val createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase,
    private val itemIdProvider: ItemIdProvider,
    private val computeItemAlphaIndexUseCase: ComputeItemAlphaIndexUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        name: String?,
        parentId: UUID?,
        isFavorite: Boolean,
        icon: ByteArray?,
        color: String?,
        position: Double? = null,
    ): LBResult<SafeItem> {
        return OSError.runCatching {
            val itemPosition = position ?: safeItemRepository.getHighestChildPosition(parentId)?.let { pos ->
                floor(pos + 1)
            } ?: 0.0

            val now = Instant.now(clock)
            val (itemKey, item) = safeItemBuilder.build(
                SafeItemBuilder.Data(
                    name = name,
                    parentId = parentId,
                    isFavorite = isFavorite,
                    icon = icon,
                    color = color,
                    id = itemIdProvider(),
                    position = itemPosition,
                    updatedAt = now,
                    indexAlpha = computeItemAlphaIndexUseCase(name).getOrThrow("Failed to compute item alpha index"),
                    createdAt = now,
                ),
            )

            val indexWordEntries: List<IndexWordEntry>? = name?.let { createIndexWordEntriesFromItemUseCase(name, item.id) }
            safeItemRepository.save(item, itemKey, indexWordEntries)
            item
        }
    }
}
