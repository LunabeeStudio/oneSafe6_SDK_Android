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

package studio.lunabee.onesafe

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.repository.datasource.SettingsDataSource
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DatastoreSettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsDataSource {
    private val autoLockInactivityDelayKey = longPreferencesKey(SettingsConstants.AutoLockInactivityDelay)
    private val autoLockAppChangeDelayKey = longPreferencesKey(SettingsConstants.AutoLockAppChangeDelay)
    private val clipboardClearDelaySecondsSettingKey = longPreferencesKey(SettingsConstants.ClipboardClearDelayMsSetting)
    private val verifyPasswordIntervalKey = stringPreferencesKey(SettingsConstants.VerifyPasswordIntervalKey)
    private val lastPasswordVerificationKey = longPreferencesKey(SettingsConstants.LastPasswordVerification)
    private val bubblesResendMessageDelayKey = longPreferencesKey(SettingsConstants.BubblesResendMessageDelay)
    private val autoLockOSKInactivityDelayKey = longPreferencesKey(SettingsConstants.AutoLockOSKInactivityDelay)
    private val autoLockOSKHiddenDelayKey = longPreferencesKey(SettingsConstants.AutoLockOSKHiddenDelay)
    private val autoBackupEnabledKey = booleanPreferencesKey(SettingsConstants.autoBackupEnabledKeyVal)
    private val autoBackupFrequencyKey = longPreferencesKey(SettingsConstants.autoBackupFrequencyKeyVal)

    override val autoLockInactivityDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockInactivityDelayKey]?.milliseconds }.firstOrNull()
                ?: SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds
        }

    override val autoLockAppChangeDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockAppChangeDelayKey]?.milliseconds }.firstOrNull()
                ?: SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds
        }

    override val autoLockOSKHiddenDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockOSKHiddenDelayKey]?.milliseconds }.firstOrNull()
                ?: autoLockAppChangeDelay
        }

    override val autoLockOSKInactivityDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockOSKInactivityDelayKey]?.milliseconds }.firstOrNull()
                ?: autoLockInactivityDelay
        }

    override val autoLockInactivityDelayFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockInactivityDelayKey]?.milliseconds ?: SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds
        }

    override val autoLockAppChangeDelayFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockAppChangeDelayKey]?.milliseconds ?: SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds
        }

    override val clipboardDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[clipboardClearDelaySecondsSettingKey]?.milliseconds }.firstOrNull()
                ?: SettingsDefaults.ClipboardClearDelayMsDefault.milliseconds
        }

    override val clipboardDelayFlow: Flow<Duration>
        get() = dataStore.data
            .map { preferences ->
                preferences[clipboardClearDelaySecondsSettingKey]?.milliseconds
                    ?: SettingsDefaults.ClipboardClearDelayMsDefault.milliseconds
            }

    override val bubblesResendMessageDelayFlow: Flow<Duration>
        get() = dataStore.data
            .map { preferences ->
                preferences[bubblesResendMessageDelayKey]?.milliseconds
                    ?: SettingsDefaults.BubblesResendMessageDelayMsDefault.milliseconds
            }
    override val autoLockOSKInactivityDelayFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockOSKInactivityDelayKey]?.milliseconds ?: autoLockAppChangeDelay
        }

    override val autoLockOSKHiddenFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockOSKHiddenDelayKey]?.milliseconds ?: autoLockInactivityDelay
        }

    override val passwordVerificationInterval: VerifyPasswordInterval
        get() = runBlocking {
            dataStore.data.map { preferences ->
                preferences[verifyPasswordIntervalKey]?.let(VerifyPasswordInterval::valueOf)
            }.firstOrNull() ?: SettingsDefaults.VerifyPasswordIntervalDefault
        }

    override val passwordVerificationIntervalFlow: Flow<VerifyPasswordInterval>
        get() = dataStore.data.map { preferences ->
            preferences[verifyPasswordIntervalKey]?.let(VerifyPasswordInterval::valueOf) ?: SettingsDefaults.VerifyPasswordIntervalDefault
        }

    override val lastPasswordVerificationTimeStamp: Long?
        get() = runBlocking {
            dataStore.data.map { preferences ->
                preferences[lastPasswordVerificationKey]
            }.firstOrNull()
        }

    override fun setBubblesResendMessageDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[bubblesResendMessageDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setAutoLockOSKInactivityDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[autoLockOSKInactivityDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setAutoLockOSKHiddenDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[autoLockOSKHiddenDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setAutoLockInactivityDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[autoLockInactivityDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setAutoLockAppChangeDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[autoLockAppChangeDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setClipboardClearDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[clipboardClearDelaySecondsSettingKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setPasswordVerificationInterval(interval: VerifyPasswordInterval) {
        runBlocking {
            dataStore.edit { settings ->
                settings[verifyPasswordIntervalKey] = interval.toString()
            }
        }
    }

    override fun setLastPasswordVerificationTimeStamp(timeStamp: Long) {
        runBlocking {
            dataStore.edit { settings ->
                settings[lastPasswordVerificationKey] = timeStamp
            }
        }
    }

    override val autoBackupEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[autoBackupEnabledKey] ?: SettingsDefaults.autoBackupEnabledDefault }

    override fun toggleAutoBackupSettings(): Boolean {
        return runBlocking {
            dataStore.edit { preferences ->
                val autoBackupEnabled = preferences[autoBackupEnabledKey] ?: SettingsDefaults.autoBackupEnabledDefault
                preferences[autoBackupEnabledKey] = !autoBackupEnabled
            }
            autoBackupEnabled.first()
        }
    }

    override val autoBackupFrequency: Duration
        get() = runBlocking {
            dataStore.data.map { preferences ->
                preferences[autoBackupFrequencyKey]?.milliseconds ?: SettingsDefaults.autoBackupFrequencyMsDefault.milliseconds
            }.firstOrNull() ?: SettingsDefaults.autoBackupFrequencyMsDefault.milliseconds
        }

    override val autoBackupFrequencyFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoBackupFrequencyKey]?.milliseconds ?: SettingsDefaults.autoBackupFrequencyMsDefault.milliseconds
        }

    override fun setAutoBackupFrequency(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[autoBackupFrequencyKey] = delay.inWholeMilliseconds
            }
        }
    }
}
