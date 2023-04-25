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

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.repository.datasource.SecurityOptionDataSource
import javax.inject.Inject
import kotlin.time.Duration

class SecurityOptionRepositoryImpl @Inject constructor(
    private val securityOptionDataSource: SecurityOptionDataSource,
) : SecurityOptionRepository {
    override val autoLockInactivityDelay: Duration
        get() = securityOptionDataSource.autoLockInactivityDelay

    override val autoLockInactivityDelayFlow: Flow<Duration>
        get() = securityOptionDataSource.autoLockInactivityDelayFlow

    override fun setAutoLockInactivityDelay(delay: Duration): Unit =
        securityOptionDataSource.setAutoLockInactivityDelay(delay)

    override val autoLockAppChangeDelay: Duration
        get() = securityOptionDataSource.autoLockAppChangeDelay

    override val autoLockAppChangeDelayFlow: Flow<Duration>
        get() = securityOptionDataSource.autoLockAppChangeDelayFlow

    override fun setAutoLockAppChangeDelay(delay: Duration): Unit =
        securityOptionDataSource.setAutoLockAppChangeDelay(delay)

    override val clipboardDelay: Duration
        get() = securityOptionDataSource.clipboardDelay

    override val clipboardDelayFlow: Flow<Duration>
        get() = securityOptionDataSource.clipboardDelayFlow

    override fun setClipboardClearDelay(delay: Duration): Unit = securityOptionDataSource.setClipboardClearDelay(delay)
}
