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

package studio.lunabee.onesafe.domain.usecase.search

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<SecureGetRecentSearchUseCase>()

/**
 * Retrieve the recent searches for the current safe
 */
class SecureGetRecentSearchUseCase @Inject constructor(
    private val recentSearchRepository: RecentSearchRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val safeRepository: SafeRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<LBFlowResult<List<String>>> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            recentSearchRepository
                .getRecentSearch(safeId)
                .map { encRecentSearch ->
                    OSError
                        .runCatching(logger) {
                            cryptoRepository.decryptRecentSearch(encRecentSearch.toList())
                        }.asFlowResult()
                }.onStart { emit(LBFlowResult.Loading()) }
        } ?: flowOf(LBFlowResult.Success(emptyList()))
    }
}
