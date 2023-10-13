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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 9:58 PM
 */

package studio.lunabee.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.FileRepository
import java.io.File
import java.util.UUID

@Module
@InstallIn(SingletonComponent::class)
object FrameworkTestModule {
    @Provides
    @BuildNumber
    fun provideBuildNumber(): Int = 0

    @Provides
    @VersionName
    fun provideVersionName(): String = "6.0.0.0"

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup)
    fun provideArchiveAutoBackupDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "test_archiveAutoBackup")
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export)
    fun provideArchiveExportedDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "test_archiveExported")
    }

    @Provides
    fun provideLoadFileCancelAllUseCase(
        fileRepository: FileRepository,
    ): LoadFileCancelAllUseCase {
        return object : LoadFileCancelAllUseCase {
            override operator fun invoke(itemId: UUID) {
                fileRepository.deleteItemDir(itemId)
            }

            override operator fun invoke() {
                fileRepository.deleteCacheDir()
            }
        }
    }
}
