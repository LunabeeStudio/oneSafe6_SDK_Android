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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.repository.datasource.SecurityOptionDataSource
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AndroidSecurityOptionDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SecurityOptionDataSource {
    private val autoLockInactivityDelayKey = longPreferencesKey(SettingsConstants.AutoLockInactivityDelay)
    private val autoLockAppChangeDelayKey = longPreferencesKey(SettingsConstants.AutoLockAppChangeDelay)
    private val clipboardClearDelaySecondsSettingKey =
        longPreferencesKey(SettingsConstants.ClipboardClearDelayMsSetting)
    private val verifyPasswordIntervalKey = stringPreferencesKey(SettingsConstants.VerifyPasswordIntervalKey)
    private val lastPasswordVerificationKey = longPreferencesKey(SettingsConstants.LastPasswordVerification)

    override val autoLockInactivityDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockInactivityDelayKey]?.milliseconds }.firstOrNull()
                ?: SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds
        }

    override val autoLockInactivityDelayFlow: Flow<Duration> = dataStore.data.map { preferences ->
        preferences[autoLockInactivityDelayKey]?.milliseconds ?: SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds
    }

    override val autoLockAppChangeDelay: Duration
        get() = runBlocking {
            dataStore.data.map { preferences -> preferences[autoLockAppChangeDelayKey]?.milliseconds }.firstOrNull()
                ?: SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds
        }

    override val autoLockAppChangeDelayFlow: Flow<Duration> = dataStore.data.map { preferences ->
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
}
