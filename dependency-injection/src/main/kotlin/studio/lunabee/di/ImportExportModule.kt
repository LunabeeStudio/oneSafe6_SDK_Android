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
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import studio.lunabee.importexport.datasource.CloudBackupEngine
import studio.lunabee.importexport.repository.DefaultBackupRepository
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.domain.qualifier.DateFormatterType
import studio.lunabee.onesafe.importexport.BackupExportEngineImpl
import studio.lunabee.onesafe.importexport.GoogleDriveEngine
import studio.lunabee.onesafe.importexport.ImportCacheDataSource
import studio.lunabee.onesafe.importexport.ImportCacheDataSourceImpl
import studio.lunabee.onesafe.importexport.ImportEngineImpl
import studio.lunabee.onesafe.importexport.ShareExportEngineImpl
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import studio.lunabee.onesafe.importexport.engine.ShareExportEngine
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.time.format.DateTimeFormatter

@Module
@InstallIn(SingletonComponent::class)
interface ImportExportModule {
    @Binds
    fun bindImportEngine(importEngineImpl: ImportEngineImpl): ImportEngine

    @Binds
    fun bindImportCacheDataSource(importCacheDataSourceImpl: ImportCacheDataSourceImpl): ImportCacheDataSource

    @Binds
    fun bindShareExportEngine(shareExportEngineImpl: ShareExportEngineImpl): ShareExportEngine

    @Binds
    @BackupType(BackupType.Type.Auto)
    fun bindAutoBackupExportEngine(backupExportEngineImpl: BackupExportEngineImpl): BackupExportEngine

    @Binds
    @BackupType(BackupType.Type.Foreground)
    fun bindForegroundBackupExportEngine(backupExportEngineImpl: BackupExportEngineImpl): BackupExportEngine

    @Binds
    fun bindCloudBackupEngine(googleDriveEngine: GoogleDriveEngine): CloudBackupEngine

    @Binds
    fun bindGoogleDriveBackupRepository(googleDriveBackupRepository: DefaultBackupRepository): CloudBackupRepository
}

@Module
@InstallIn(SingletonComponent::class)
class ImportDateFormatterModule {
    @Provides
    @DateFormatterType(type = DateFormatterType.Type.IsoInstant)
    fun provideArchiveDateFormatter(): DateTimeFormatter = DateTimeFormatter.ISO_INSTANT
}
