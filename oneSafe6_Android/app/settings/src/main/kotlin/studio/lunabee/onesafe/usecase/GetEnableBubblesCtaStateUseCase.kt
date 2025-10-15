/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/20/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 5:21 PM
 */

package studio.lunabee.onesafe.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class GetEnableBubblesCtaStateUseCase @Inject constructor(
    private val appSettingsRepository: SafeSettingsRepository,
    private val appVisitRepository: AppVisitRepository,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CtaState> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appSettingsRepository.bubblesHomeCardCtaState(safeId).transformLatest { currentState ->
                emit(currentState)

                when (currentState) {
                    is CtaState.DismissedAt -> {} // keep dismiss
                    CtaState.Hidden,
                    is CtaState.VisibleSince,
                    -> { // observe new value and update on value change
                        appVisitRepository
                            .hasDoneOnBoardingBubblesFlow(safeId)
                            .distinctUntilChanged()
                            .collect { hasDoneOnBoardingBubbles ->
                                val newState = when (hasDoneOnBoardingBubbles) {
                                    true -> CtaState.Hidden
                                    false -> CtaState.VisibleSince(Instant.now(clock))
                                }

                                if (newState::class != currentState::class) {
                                    appSettingsRepository.setBubblesHomeCardCtaState(safeId, newState)
                                }
                            }
                    }
                }
            }
        } ?: flowOf<CtaState>(CtaState.Hidden)
    }
}
