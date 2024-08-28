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
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.onesafe.domain.common.RandomUuidProvider
import studio.lunabee.onesafe.domain.common.UuidProvider
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteBackupsUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCaseImpl
import studio.lunabee.onesafe.importexport.usecase.DeleteBackupsUseCaseImpl
import studio.lunabee.onesafe.migration.LoginAndMigrateUseCase
import studio.lunabee.onesafe.migration.utils.AndroidMultiSafeMigrationProvider
import studio.lunabee.onesafe.storage.migration.RoomMigration12to13
import java.io.File
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun provideDoubleRatchetEngine(
        localDatasource: DoubleRatchetLocalDatasource,
        doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    ): DoubleRatchetEngine = DoubleRatchetEngine(
        doubleRatchetKeyRepository = doubleRatchetKeyRepository,
        doubleRatchetLocalDatasource = localDatasource,
    )

    @Provides
    fun provideUuidProvider(): UuidProvider = RandomUuidProvider()

    @Provides
    @FileDispatcher
    fun providesFileDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @RemoteDispatcher
    fun providesRemoteDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    fun providesClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun providesKotlinClock(): kotlinx.datetime.Clock = kotlinx.datetime.Clock.System

    @Provides
    @InternalDir(InternalDir.Type.Backups)
    fun providesInternalDirBackups(
        @ApplicationContext context: Context,
    ): File = File(context.filesDir, "backups")

    @Provides
    @InternalDir(InternalDir.Type.Cache)
    fun providesInternalDirCache(
        @ApplicationContext context: Context,
    ): File = context.cacheDir
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModuleBinds {

    @Binds
    internal abstract fun bindsSafeMigrationProvider(
        safeMigrationProviderUseCase: AndroidMultiSafeMigrationProvider,
    ): RoomMigration12to13.MultiSafeMigrationProvider

    @Binds
    internal abstract fun bindsAutoLockInactivityGetRemainingTimeUseCase(
        autoLockInactivityGetRemainingTimeUseCaseImpl: AutoLockInactivityGetRemainingTimeUseCaseImpl,
    ): AutoLockInactivityGetRemainingTimeUseCase

    @Binds
    internal abstract fun bindsLoginUseCase(
        loginUseCase: LoginAndMigrateUseCase,
    ): LoginUseCase

    @Binds
    internal abstract fun bindsDeleteBackupsUseCase(
        deleteBackupsUseCase: DeleteBackupsUseCaseImpl,
    ): DeleteBackupsUseCase
}
