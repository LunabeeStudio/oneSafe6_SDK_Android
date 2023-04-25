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
 * Created by Lunabee Studio / Date - 4/11/2023 - for the oneSafe6 SDK.
 * Last modified 4/11/23, 10:45 AM
 */

package studio.lunabee.onesafe.domain.usecase.autolock

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Retrieve the user param delay before automatically locking app when there is no user interactions
 */
class AutoLockGetInactivityDelayUseCase @Inject constructor(
    private val securityOptionRepository: SecurityOptionRepository,
) {

    operator fun invoke(): Duration = securityOptionRepository.autoLockInactivityDelay

    fun getAsFlow(): Flow<Duration> = securityOptionRepository.autoLockInactivityDelayFlow
}
