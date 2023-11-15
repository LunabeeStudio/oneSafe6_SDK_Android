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
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SecurityOptionRepository
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository
import studio.lunabee.onesafe.repository.datasource.SettingsDataSource
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

class SettingsRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
) : SecurityOptionRepository, AutoBackupSettingsRepository {
    override val autoLockInactivityDelay: Duration
        get() = settingsDataSource.autoLockInactivityDelay

    override val autoLockInactivityDelayFlow: Flow<Duration>
        get() = settingsDataSource.autoLockInactivityDelayFlow

    override fun setAutoLockInactivityDelay(delay: Duration) {
        settingsDataSource.autoLockInactivityDelay = delay
    }

    override val autoLockAppChangeDelay: Duration
        get() = settingsDataSource.autoLockAppChangeDelay

    override val autoLockAppChangeDelayFlow: Flow<Duration>
        get() = settingsDataSource.autoLockAppChangeDelayFlow

    override fun setAutoLockAppChangeDelay(delay: Duration) {
        settingsDataSource.autoLockAppChangeDelay = delay
    }

    override val clipboardDelay: Duration
        get() = settingsDataSource.clipboardDelay

    override val clipboardDelayFlow: Flow<Duration>
        get() = settingsDataSource.clipboardDelayFlow

    override fun setClipboardClearDelay(delay: Duration) {
        settingsDataSource.clipboardDelay = delay
    }

    override val verifyPasswordInterval: VerifyPasswordInterval
        get() = settingsDataSource.passwordVerificationInterval

    override val verifyPasswordIntervalFlow: Flow<VerifyPasswordInterval>
        get() = settingsDataSource.passwordVerificationIntervalFlow

    override fun setPasswordInterval(passwordInterval: VerifyPasswordInterval) {
        settingsDataSource.passwordVerificationInterval = passwordInterval
    }

    override val lastPasswordVerificationInstant: Instant?
        get() = settingsDataSource.lastPasswordVerificationInstant

    override fun setLastPasswordVerification(instant: Instant) {
        settingsDataSource.setLastPasswordVerificationInstant(instant)
    }

    override val bubblesResendMessageDelayFlow: Flow<Duration>
        get() = settingsDataSource.bubblesResendMessageDelayFlow

    override fun setBubblesResendMessageDelay(delay: Duration) {
        settingsDataSource.setBubblesResendMessageDelay(delay)
    }

    override val autoLockOSKInactivityDelay: Duration
        get() = settingsDataSource.autoLockOSKInactivityDelay

    override val autoLockOSKInactivityDelayFlow: Flow<Duration>
        get() = settingsDataSource.autoLockOSKInactivityDelayFlow

    override fun setAutoLockOSKInactivityDelay(delay: Duration) {
        settingsDataSource.autoLockOSKInactivityDelay = delay
    }

    override val autoLockOSKHiddenDelay: Duration
        get() = settingsDataSource.autoLockOSKHiddenDelay

    override val autoLockOSKHiddenDelayFlow: Flow<Duration>
        get() = settingsDataSource.autoLockOSKHiddenFlow

    override fun setAutoLockOSKHiddenDelay(delay: Duration) {
        settingsDataSource.autoLockOSKHiddenDelay = delay
    }

    override val autoBackupEnabled: Flow<Boolean>
        get() = settingsDataSource.autoBackupEnabled

    override val autoBackupFrequency: Duration
        get() = settingsDataSource.autoBackupFrequency
    override val autoBackupFrequencyFlow: Flow<Duration>
        get() = settingsDataSource.autoBackupFrequencyFlow

    override fun toggleAutoBackupSettings(): Boolean =
        settingsDataSource.toggleAutoBackupSettings()

    override fun setAutoBackupFrequency(delay: Duration) {
        settingsDataSource.autoBackupFrequency = delay
    }

    override val cloudBackupEnabled: Flow<Boolean>
        get() = settingsDataSource.cloudBackupEnabled

    override suspend fun setCloudBackupSettings(enabled: Boolean): Unit =
        settingsDataSource.setCloudBackupSettings(enabled)

    override val keepLocalBackupEnabled: Flow<Boolean>
        get() = settingsDataSource.keepLocalBackupEnabled

    override suspend fun toggleKeepLocalBackupSettings(): Boolean =
        settingsDataSource.toggleKeepLocalBackupSettings()
}
