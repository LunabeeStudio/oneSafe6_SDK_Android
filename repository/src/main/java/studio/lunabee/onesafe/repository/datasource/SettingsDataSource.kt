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

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutStyle
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import java.time.Instant
import kotlin.time.Duration

interface SettingsDataSource {
    var autoLockInactivityDelay: Duration
    val autoLockInactivityDelayFlow: Flow<Duration>

    var autoLockAppChangeDelay: Duration
    val autoLockAppChangeDelayFlow: Flow<Duration>

    var clipboardDelay: Duration
    val clipboardDelayFlow: Flow<Duration>

    var passwordVerificationInterval: VerifyPasswordInterval
    val passwordVerificationIntervalFlow: Flow<VerifyPasswordInterval>

    val lastPasswordVerificationInstant: Instant?
    fun setLastPasswordVerificationInstant(instant: Instant)

    val bubblesResendMessageDelayFlow: Flow<Duration>
    fun setBubblesResendMessageDelay(delay: Duration)
    var autoLockOSKInactivityDelay: Duration
    val autoLockOSKInactivityDelayFlow: Flow<Duration>
    var autoLockOSKHiddenDelay: Duration
    val autoLockOSKHiddenFlow: Flow<Duration>

    fun toggleAutoBackupSettings(): Boolean
    val autoBackupEnabled: Flow<Boolean>
    val autoBackupFrequencyFlow: Flow<Duration>
    var autoBackupFrequency: Duration

    val cloudBackupEnabled: Flow<Boolean>
    suspend fun setCloudBackupSettings(enabled: Boolean)

    val keepLocalBackupEnabled: Flow<Boolean>
    suspend fun setKeepLocalBackupSettings(enabled: Boolean)

    val itemOrdering: Flow<ItemOrder>
    suspend fun setItemOrdering(order: ItemOrder)

    val itemsLayoutStyle: Flow<ItemsLayoutStyle>
    suspend fun setItemsLayoutStyle(style: ItemsLayoutStyle)
}
