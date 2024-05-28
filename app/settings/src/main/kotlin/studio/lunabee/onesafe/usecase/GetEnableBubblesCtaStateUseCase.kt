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
 * Created by Lunabee Studio / Date - 4/23/2024 - for the oneSafe6 SDK.
 * Last modified 4/23/24, 2:41 PM
 */

package studio.lunabee.onesafe.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.visits.OSAppVisit
import studio.lunabee.onesafe.visits.OSPreferenceTips
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class GetEnableBubblesCtaStateUseCase @Inject constructor(
    private val osAppSettings: OSAppSettings,
    private val osAppVisit: OSAppVisit,
    private val clock: Clock,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<CtaState> {
        return osAppSettings.bubblesHomeCardCtaState.transformLatest { currentState ->
            emit(currentState)

            when (currentState) {
                is CtaState.DismissedAt -> {} // keep dismiss
                CtaState.Hidden,
                is CtaState.VisibleSince,
                -> { // observe new value and update on value change
                    osAppVisit.getAsFlow(OSPreferenceTips.HasDoneOnBoardingBubbles)
                        .distinctUntilChanged()
                        .collect { hasDoneOnBoardingBubbles ->
                            val newState = when (hasDoneOnBoardingBubbles) {
                                true -> CtaState.Hidden
                                false -> CtaState.VisibleSince(Instant.now(clock))
                            }

                            if (newState::class != currentState::class) {
                                osAppSettings.setBubblesHomeCardCtaState(newState)
                            }
                        }
                }
            }
        }
    }
}
