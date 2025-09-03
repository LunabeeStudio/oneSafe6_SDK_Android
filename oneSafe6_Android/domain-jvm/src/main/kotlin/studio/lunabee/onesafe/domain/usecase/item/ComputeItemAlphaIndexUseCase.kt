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
 * Created by Lunabee Studio / Date - 12/8/2023 - for the oneSafe6 SDK.
 * Last modified 12/8/23, 5:04 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.get
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Compute the expected alphabetic index for the item name.
 */
class ComputeItemAlphaIndexUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val itemDecryptUseCase: ItemDecryptUseCase,
    private val cleanForAlphaIndexingUseCase: CleanForAlphaIndexingUseCase,
    private val safeRepository: SafeRepository,
) {
    /**
     * @param itemName the name to compute the index on
     * @param currentIndex optional current index to avoid creating a new index if not necessary
     */
    suspend operator fun invoke(itemName: String?, currentIndex: Double? = null): LBResult<Double> = OSError.runCatching(
        mapErr = { OSDomainError.Code.ALPHA_INDEX_COMPUTE_FAILED.get(cause = it) },
    ) {
        val safeId = safeRepository.currentSafeId()
        val cleanName = cleanForAlphaIndexingUseCase(itemName.orEmpty())
        val count = safeItemRepository.getSafeItemsCount(safeId)
        val range = safeItemRepository.getAlphaIndexRange(safeId)

        if (cleanName.isEmpty()) {
            emptyIndex(count - 1, safeId)
        } else {
            binarySearch(
                initEnd = count - 1,
                name = cleanName,
                range = range,
                currentIndex = currentIndex,
                safeId = safeId,
            )
        }
    }

    private suspend fun emptyIndex(lastIndex: Int, safeId: SafeId): Double {
        val lastItem = safeItemRepository.getItemNameWithIndexAt(lastIndex, safeId)
        val lastItemName = lastItem?.encName
            ?.let { itemDecryptUseCase(it, lastItem.id, String::class).data!! }
            ?.let { cleanForAlphaIndexingUseCase(it) }
        return if (lastItemName.isNullOrEmpty()) {
            lastItem?.indexAlpha ?: 0.0
        } else {
            floor(lastItem.indexAlpha + 1.0)
        }
    }

    private suspend fun binarySearch(
        initEnd: Int,
        name: String,
        range: Pair<Double, Double>,
        currentIndex: Double?,
        safeId: SafeId,
    ): Double {
        var start = 0
        var end = initEnd
        // Init with possible new max/min order
        var previous = ceil(range.first - 1.0)
        var next = floor(range.second + 1.0)
        while (start <= end && next != previous) {
            val mid = (start + (end - start) / 2f).toInt()
            val midItemNameWithIndex = safeItemRepository.getItemNameWithIndexAt(mid, safeId)
            val midItemPlainName = getItemPlainName(midItemNameWithIndex)

            when {
                midItemNameWithIndex == null -> {
                    next = 0.0
                    previous = 0.0
                }
                midItemPlainName == name -> {
                    next = midItemNameWithIndex.indexAlpha
                    previous = midItemNameWithIndex.indexAlpha
                }
                midItemPlainName < name -> {
                    start = mid + 1
                    previous = midItemNameWithIndex.indexAlpha
                }
                else -> {
                    end = mid - 1
                    next = midItemNameWithIndex.indexAlpha
                }
            }
        }

        return when {
            previous == range.second -> next // new last item
            next == range.first -> previous // new first item
            previous == currentIndex -> currentIndex // item position does not changed (lower bound)
            next == currentIndex -> currentIndex // item position does not changed (higher bound)
            else -> (next + previous) / 2f // compute new position between 2 items
        }
    }

    private suspend fun getItemPlainName(midItemNameWithIndex: ItemNameWithIndex?): String {
        return midItemNameWithIndex?.encName
            ?.let { itemDecryptUseCase(it, midItemNameWithIndex.id, String::class).getOrThrow() }
            ?.let { cleanForAlphaIndexingUseCase(it) }
            .orEmpty()
    }
}
