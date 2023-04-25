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

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.domain.engine.ExportEngine
import studio.lunabee.onesafe.domain.engine.ImportEngine
import studio.lunabee.onesafe.domain.qualifier.DateFormatterType
import studio.lunabee.onesafe.importexport.ExportCacheDataSource
import studio.lunabee.onesafe.importexport.ExportCacheDataSourceImpl
import studio.lunabee.onesafe.importexport.ExportEngineImpl
import studio.lunabee.onesafe.importexport.ImportCacheDataSource
import studio.lunabee.onesafe.importexport.ImportCacheDataSourceImpl
import studio.lunabee.onesafe.importexport.ImportEngineImpl
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ImportExportModule::class],
)
interface ImportExportTestModule {
    @Binds
    @Singleton
    fun bindImportEngine(importEngineImpl: ImportEngineImpl): ImportEngine

    @Binds
    @Singleton
    fun bindExportEngine(exportEngineImpl: ExportEngineImpl): ExportEngine

    @Binds
    @Singleton
    fun bindImportCacheDataSource(importCacheDataSourceImpl: ImportCacheDataSourceImpl): ImportCacheDataSource

    @Binds
    @Singleton
    fun bindExportCacheDataSource(exportCacheDataSourceImpl: ExportCacheDataSourceImpl): ExportCacheDataSource
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ImportDateFormatterModule::class],
)
class ImportDateFormatterTestModule {
    @Provides
    @DateFormatterType(type = DateFormatterType.Type.IsoInstant)
    fun provideArchiveDateFormatter(): DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
}
