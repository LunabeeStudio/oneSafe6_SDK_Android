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
 * Created by Lunabee Studio / Date - 6/7/2023 - for the oneSafe6 SDK.
 * Last modified 6/7/23, 9:58 AM
 */

package studio.lunabee.onesafe.domain.usecase.support

import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Increment the app visit counter if "ask for support" is not yet displayed and make "should ask for support" visible depending on
 * [ShouldAskForSupportUseCase].
 */
class IncrementVisitForAskingForSupportUseCase @Inject constructor(
    private val supportOSRepository: SupportOSRepository,
    private val shouldAskForSupportUseCase: ShouldAskForSupportUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        if (supportOSRepository.visibleSince.first() == null) {
            supportOSRepository.incrementAppVisit()
            if (shouldAskForSupportUseCase()) {
                supportOSRepository.setVisibleSince(Instant.now(clock))
            }
        }
    }
}
