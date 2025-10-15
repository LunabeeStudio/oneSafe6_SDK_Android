/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.lunabee.onesafe.repository.datasource.GlobalSettingsLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeSettingsLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SupportOSDataSource
import studio.lunabee.onesafe.storage.datasource.SettingsLocalDataSource
import studio.lunabee.onesafe.support.SupportOSDataStore
import studio.lunabee.onesafe.visits.GlobalSettingsDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {
    @Binds
    @Singleton
    fun bindsSafeSettingsLocalDataSource(settingsLocalDataSourceImpl: SettingsLocalDataSource): SafeSettingsLocalDataSource

    @Binds
    @Singleton
    fun bindsGlobalSettingsLocalDataSource(globalSettingsDataStore: GlobalSettingsDataStore): GlobalSettingsLocalDataSource

    @Binds
    @Singleton
    fun bindSupportOSDataSource(supportOSDataStore: SupportOSDataStore): SupportOSDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object SettingsPreferenceDataStoreModule {

    private const val SettingsPrefDataStore: String = "4077d1e6-cc0f-47d3-8fff-2d79aaac6ced"

    @Provides
    @Singleton
    fun providePreferenceDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(SettingsPrefDataStore)
    }
}
