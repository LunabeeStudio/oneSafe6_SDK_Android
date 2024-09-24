/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/14/2024 - for the oneSafe6 SDK.
 * Last modified 6/14/24, 9:13 AM
 */

package studio.lunabee.onesafe.domain.usecase.item

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import javax.inject.Inject

/**
 * Get the number of favorite items for the current safe
 */
class CountAllFavoriteUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Int> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let { safeItemRepository.countAllFavoriteFlow(safeId) } ?: flowOf(0)
    }
}
