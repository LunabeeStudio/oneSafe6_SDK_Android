/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Store support oS visibleSince date to now if already visible
 */
class MigrationFromV8ToV9 @Inject constructor(
    private val shouldAskForSupportUseCase: ShouldAskForSupportUseCase,
    private val supportOSRepository: SupportOSRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): LBResult<Unit> {
        if (shouldAskForSupportUseCase()) {
            supportOSRepository.setVisibleSince(Instant.now(clock))
        }
        return LBResult.Success(Unit)
    }
}
