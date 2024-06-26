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
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.model.LocalCtaState
import studio.lunabee.onesafe.model.LocalCtaStateMap
import studio.lunabee.onesafe.model.edit
import javax.inject.Inject

class OSAppSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val ctaDataStore: DataStore<LocalCtaStateMap>,
) {

    private val materialYouSettingKey = booleanPreferencesKey(SettingsConstants.MaterialYouSetting)

    val materialYouSetting: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[materialYouSettingKey] ?: SettingsDefaults.MaterialYouSettingDefault }

    private val automationSettingKey = booleanPreferencesKey(SettingsConstants.AutomationSetting)

    val automationSetting: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[automationSettingKey] ?: SettingsDefaults.AutomationSettingDefault }

    private val displayShareWarningKey = booleanPreferencesKey(SettingsConstants.DisplayShareWarning)

    val displayShareWarning: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[displayShareWarningKey] ?: SettingsDefaults.DisplayShareWarningDefault }

    private val allowScreenshotKey = booleanPreferencesKey(SettingsConstants.AllowScreenshotSetting)

    val allowScreenshotFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[allowScreenshotKey] ?: SettingsDefaults.AllowScreenshotSettingDefault }

    private val bubblesPreviewKey = booleanPreferencesKey(SettingsConstants.BubblesPreview)

    val bubblesPreview: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[bubblesPreviewKey] ?: SettingsDefaults.BubblesPreviewDefault }

    val bubblesHomeCardCtaState: Flow<CtaState>
        get() = ctaDataStore.data.map { it[SettingsConstants.bubblesCtaKeyVal].toCtaState() }

    private val cameraSystemKey = stringPreferencesKey(SettingsConstants.CameraSystem)
    val cameraSystemFlow: Flow<CameraSystem> = dataStore.data
        .map { preferences -> CameraSystem.valueOf(preferences[cameraSystemKey] ?: SettingsDefaults.CameraSystemDefault.name) }
    val cameraSystem: CameraSystem
        get() = runBlocking { cameraSystemFlow.first() }

    suspend fun allowScreenshot(): Boolean = dataStore.data
        .map { preferences -> preferences[allowScreenshotKey] ?: SettingsDefaults.AllowScreenshotSettingDefault }.first()

    suspend fun toggleMaterialYouSetting() {
        dataStore.edit { preferences ->
            val materialYouSetting = preferences[materialYouSettingKey] ?: SettingsDefaults.MaterialYouSettingDefault
            preferences[materialYouSettingKey] = !materialYouSetting
        }
    }

    suspend fun toggleAutomationSetting() {
        dataStore.edit { preferences ->
            val automationSettings = preferences[automationSettingKey] ?: SettingsDefaults.AutomationSettingDefault
            preferences[automationSettingKey] = !automationSettings
        }
    }

    suspend fun disableShareWarningDisplay() {
        dataStore.edit { preferences ->
            preferences[displayShareWarningKey] = false
        }
    }

    suspend fun toggleAllowScreenshotSetting() {
        dataStore.edit { preferences ->
            val allowScreenshot = preferences[allowScreenshotKey] ?: SettingsDefaults.AllowScreenshotSettingDefault
            preferences[allowScreenshotKey] = !allowScreenshot
        }
    }

    private val migrationVersionSettingKey = intPreferencesKey(SettingsConstants.MigrationVersionSetting)
    suspend fun getMigrationVersionSetting(): Int? = dataStore.data.map { preferences -> preferences[migrationVersionSettingKey] }
        .firstOrNull()

    suspend fun setMigrationVersionSetting(version: Int?) {
        if (version != null) {
            dataStore.edit { preferences -> preferences[migrationVersionSettingKey] = version }
        } else {
            dataStore.edit { preferences -> preferences.remove(migrationVersionSettingKey) }
        }
    }

    suspend fun setBubblesPreviewSettings(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[bubblesPreviewKey] = value
        }
    }

    suspend fun setCameraSystem(value: CameraSystem) {
        dataStore.edit { preferences ->
            preferences[cameraSystemKey] = value.name
        }
    }

    suspend fun setBubblesHomeCardCtaState(ctaState: CtaState) {
        ctaDataStore.edit { it[SettingsConstants.bubblesCtaKeyVal] = LocalCtaState.fromCtaState(ctaState) }
    }
}
