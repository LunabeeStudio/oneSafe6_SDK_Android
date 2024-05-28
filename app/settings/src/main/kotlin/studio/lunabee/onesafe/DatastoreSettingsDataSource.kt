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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lunabee.lbextensions.enumValueOfOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.model.LocalCtaState
import studio.lunabee.onesafe.model.LocalCtaStateMap
import studio.lunabee.onesafe.model.edit
import studio.lunabee.onesafe.repository.datasource.SettingsDataSource
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DatastoreSettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val ctaDataStore: DataStore<LocalCtaStateMap>,
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
    private val autoBackupMaxNumberKey = intPreferencesKey(SettingsConstants.autoBackupMaxNumberKeyVal)
    private val cloudBackupEnabledKey = booleanPreferencesKey(SettingsConstants.cloudBackupEnabledKeyVal)
    private val keepLocalBackupEnabledKey = booleanPreferencesKey(SettingsConstants.keepLocalBackupEnabledKeyVal)
    private val itemOrderingKey = stringPreferencesKey(SettingsConstants.itemOrderingKeyVal)
    private val itemsLayoutSettingKey = stringPreferencesKey(SettingsConstants.itemsLayoutSettingKeyVal)

    override var autoLockInactivityDelay: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = autoLockInactivityDelayKey,
        defaultValue = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
    )

    override var autoLockAppChangeDelay: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = autoLockAppChangeDelayKey,
        defaultValue = SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds,
    )

    override var autoLockOSKHiddenDelay: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = autoLockOSKHiddenDelayKey,
        defaultValue = autoLockAppChangeDelay,
    )

    override var autoLockOSKInactivityDelay: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = autoLockOSKInactivityDelayKey,
        defaultValue = autoLockInactivityDelay,
    )

    override val autoLockInactivityDelayFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockInactivityDelayKey]?.milliseconds ?: SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds
        }

    override val autoLockAppChangeDelayFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoLockAppChangeDelayKey]?.milliseconds ?: SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds
        }

    override var clipboardDelay: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = clipboardClearDelaySecondsSettingKey,
        defaultValue = SettingsDefaults.ClipboardClearDelayMsDefault.milliseconds,
    )

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

    override var passwordVerificationInterval: VerifyPasswordInterval by blockingEnumDatastore(
        dataStore,
        verifyPasswordIntervalKey,
        SettingsDefaults.VerifyPasswordIntervalDefault,
    )

    override val passwordVerificationIntervalFlow: Flow<VerifyPasswordInterval>
        get() = dataStore.data.map { preferences ->
            preferences[verifyPasswordIntervalKey]?.let(VerifyPasswordInterval::valueOf) ?: SettingsDefaults.VerifyPasswordIntervalDefault
        }

    override val lastPasswordVerificationInstant: Instant?
        get() = runBlocking {
            dataStore.data.map { preferences ->
                preferences[lastPasswordVerificationKey]?.let { Instant.ofEpochMilli(it) }
            }.firstOrNull()
        }

    override fun setBubblesResendMessageDelay(delay: Duration) {
        runBlocking {
            dataStore.edit { settings ->
                settings[bubblesResendMessageDelayKey] = delay.inWholeMilliseconds
            }
        }
    }

    override fun setLastPasswordVerificationInstant(instant: Instant) {
        runBlocking {
            dataStore.edit { settings ->
                settings[lastPasswordVerificationKey] = instant.toEpochMilli()
            }
        }
    }

    override val autoBackupEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[autoBackupEnabledKey] ?: SettingsDefaults.autoBackupEnabledDefault }

    override fun toggleAutoBackupSettings(): Boolean = runBlocking {
        dataStore.edit { preferences ->
            val autoBackupEnabled = preferences[autoBackupEnabledKey] ?: SettingsDefaults.autoBackupEnabledDefault
            preferences[autoBackupEnabledKey] = !autoBackupEnabled
        }
        autoBackupEnabled.first()
    }

    override var autoBackupFrequency: Duration by blockingDurationDatastore(
        dataStore = dataStore,
        key = autoBackupFrequencyKey,
        defaultValue = SettingsDefaults.autoBackupFrequencyMsDefault.milliseconds,
    )

    override fun updateAutoBackupMaxNumber(updatedValue: Int): Unit = runBlocking {
        dataStore.edit { preferences ->
            preferences[autoBackupMaxNumberKey] = updatedValue
        }
    }

    override val autoBackupMaxNumberFlow: Flow<Int> =
        dataStore.data.map { preferences ->
            preferences[autoBackupMaxNumberKey] ?: SettingsDefaults.autoBackupMaxNumberDefault
        }

    override val autoBackupMaxNumber: Int
        get() = runBlocking {
            dataStore.data.map { preferences ->
                preferences[autoBackupMaxNumberKey]
            }.firstOrNull() ?: SettingsDefaults.autoBackupMaxNumberDefault
        }

    override val autoBackupFrequencyFlow: Flow<Duration>
        get() = dataStore.data.map { preferences ->
            preferences[autoBackupFrequencyKey]?.milliseconds ?: SettingsDefaults.autoBackupFrequencyMsDefault.milliseconds
        }

    override val cloudBackupEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[cloudBackupEnabledKey] ?: SettingsDefaults.cloudBackupEnabledDefault }

    override suspend fun setCloudBackupSettings(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[cloudBackupEnabledKey] = enabled
        }
    }

    override val keepLocalBackupEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[keepLocalBackupEnabledKey] ?: SettingsDefaults.keepLocalBackupEnabledDefault }

    override suspend fun setKeepLocalBackupSettings(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[keepLocalBackupEnabledKey] = enabled
        }
    }

    override val itemOrdering: Flow<ItemOrder> = dataStore.data
        .map { preferences -> enumValueOfOrNull<ItemOrder>(preferences[itemOrderingKey]) ?: SettingsDefaults.itemOrderingDefault }

    override suspend fun setItemOrdering(order: ItemOrder) {
        dataStore.edit { preferences ->
            preferences[itemOrderingKey] = order.name
        }
    }

    override val itemsLayoutSetting: Flow<ItemsLayoutSettings> = dataStore.data
        .map { preferences ->
            enumValueOfOrNull<ItemsLayoutSettings>(preferences[itemsLayoutSettingKey]) ?: SettingsDefaults.ItemsLayoutSettingDefault
        }

    override suspend fun setItemsLayoutSetting(style: ItemsLayoutSettings) {
        dataStore.edit { preferences ->
            preferences[itemsLayoutSettingKey] = style.name
        }
    }

    override val enableAutoBackupCtaState: Flow<CtaState>
        get() = ctaDataStore.data.map { it[SettingsConstants.backupCtaKeyVal].toCtaState() }

    override suspend fun setEnableAutoBackupCtaState(ctaState: CtaState) {
        ctaDataStore.edit { it[SettingsConstants.backupCtaKeyVal] = LocalCtaState.fromCtaState(ctaState) }
    }
}
