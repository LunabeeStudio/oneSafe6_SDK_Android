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

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import java.time.Instant
import kotlin.time.Duration

interface SecurityOptionRepository {
    val autoLockInactivityDelay: Duration
    val autoLockInactivityDelayFlow: Flow<Duration>
    fun setAutoLockInactivityDelay(delay: Duration)

    val autoLockAppChangeDelay: Duration
    val autoLockAppChangeDelayFlow: Flow<Duration>
    fun setAutoLockAppChangeDelay(delay: Duration)

    val clipboardDelay: Duration
    val clipboardDelayFlow: Flow<Duration>
    fun setClipboardClearDelay(delay: Duration)

    val verifyPasswordInterval: VerifyPasswordInterval
    val verifyPasswordIntervalFlow: Flow<VerifyPasswordInterval>

    fun setPasswordInterval(passwordInterval: VerifyPasswordInterval)

    val lastPasswordVerificationInstant: Instant?
    fun setLastPasswordVerification(instant: Instant)

    val bubblesResendMessageDelayFlow: Flow<Duration>
    fun setBubblesResendMessageDelay(delay: Duration)

    val autoLockOSKInactivityDelay: Duration
    val autoLockOSKInactivityDelayFlow: Flow<Duration>
    fun setAutoLockOSKInactivityDelay(delay: Duration)

    val autoLockOSKHiddenDelay: Duration
    val autoLockOSKHiddenDelayFlow: Flow<Duration>
    fun setAutoLockOSKHiddenDelay(delay: Duration)
}
