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
 * Created by Lunabee Studio / Date - 12/14/2023 - for the oneSafe6 SDK.
 * Last modified 12/14/23, 3:52 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import javax.inject.Inject

class GetPagerItemFavoriteUseCase @Inject constructor(
    private val itemSettingsRepository: ItemSettingsRepository,
    private val safeItemRepository: SafeItemRepository,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(pagingConfig: PagingConfig): Flow<PagingData<SafeItem>> = safeRepository
        .currentSafeIdFlow()
        .flatMapLatest { safeId ->
            safeId?.let {
                itemSettingsRepository.itemOrdering(safeId).flatMapLatest { itemOrder ->
                    safeItemRepository.getPagerItemFavorite(
                        pagingConfig,
                        itemOrder,
                        safeId,
                    )
                }
            } ?: flowOf(PagingData.empty())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun withIdentifier(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>> = safeRepository
        .currentSafeIdFlow()
        .flatMapLatest { safeId ->
            safeId?.let {
                itemSettingsRepository.itemOrdering(safeId).flatMapLatest { itemOrder ->
                    safeItemRepository.getPagerItemFavoriteWithIdentifier(
                        pagingConfig,
                        itemOrder,
                        safeId,
                    )
                }
            } ?: flowOf(PagingData.empty())
        }
}
