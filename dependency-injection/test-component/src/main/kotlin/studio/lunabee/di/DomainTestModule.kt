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
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.domain.common.SafeIdProvider
import studio.lunabee.onesafe.domain.common.UuidProvider
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.UpdatePanicButtonWidgetUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteBackupsUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCaseImpl
import studio.lunabee.onesafe.importexport.usecase.DeleteBackupsUseCaseImpl
import studio.lunabee.onesafe.migration.LoginAndMigrateUseCase
import studio.lunabee.onesafe.test.IncrementalIdProvider
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.firstSafeId
import java.io.File
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WidgetTestModule {
    @Provides
    fun providesUpdateWidgetUseCase(): UpdatePanicButtonWidgetUseCase = object : UpdatePanicButtonWidgetUseCase {
        override suspend fun invoke() {}
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DomainModule::class, DomainModuleBinds::class],
)
object DomainTestModule {
    @Provides
    @Singleton
    fun provideItemIdProvider(): ItemIdProvider = ItemIdProvider(IncrementalIdProvider())

    @Provides
    @Singleton
    fun provideFieldIdProvider(): FieldIdProvider = FieldIdProvider(IncrementalIdProvider())

    @Provides
    @Singleton
    fun provideIconIdProvider(): IconIdProvider = IconIdProvider(IncrementalIdProvider())

    @Provides
    @Singleton
    fun provideFileIdProvider(): FileIdProvider = FileIdProvider(IncrementalIdProvider())

    @Provides
    @Singleton
    fun provideMessageIdProvider(): MessageIdProvider = MessageIdProvider(IncrementalIdProvider())

    // TODO <multisafe> use static ID for now -> find a way to easily test multisafe feature
    @Provides
    @Singleton
    fun provideSafeIdProvider(): SafeIdProvider = SafeIdProvider(
        StaticIdProvider.apply {
            id = firstSafeId.id
        },
    )

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
    fun provideDoubleRatchetEngine(
        localDatasource: DoubleRatchetLocalDatasource,
        doubleRatchetKeyRepository: DoubleRatchetKeyRepository,
    ): DoubleRatchetEngine = DoubleRatchetEngine(
        doubleRatchetKeyRepository = doubleRatchetKeyRepository,
        doubleRatchetLocalDatasource = localDatasource,
    )

    @Provides
    @Singleton
    fun providesClock(): Clock = OSTestConfig.clock

    @Provides
    @Singleton
    fun providesKotlinClock(): kotlinx.datetime.Clock = object : kotlinx.datetime.Clock {
        override fun now(): Instant {
            return Instant.fromEpochMilliseconds(0)
        }
    }

    @Provides
    @InternalDir(InternalDir.Type.Backups)
    fun providesInternalDirBackups(
        @ApplicationContext context: Context,
    ): File = File(context.cacheDir, "test_backups")

    @Provides
    @InternalDir(InternalDir.Type.Cache)
    fun providesInternalDirCache(
        @ApplicationContext context: Context,
    ): File = context.cacheDir

    @Provides
    fun provideUuidProvider(): UuidProvider = IncrementalIdProvider()

    @Provides
    fun providesAutoLockInactivityGetRemainingTimeUseCase(
        securitySettingsRepository: SecuritySettingsRepository,
        autoLockRepository: AutoLockRepository,
        clock: Clock,
        safeRepository: SafeRepository,
    ): AutoLockInactivityGetRemainingTimeUseCase {
        return AutoLockInactivityGetRemainingTimeUseCaseImpl(
            securitySettingsRepository,
            autoLockRepository,
            clock,
            safeRepository,
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainTestModuleBinds {

    @Binds
    internal abstract fun bindsLoginUseCase(
        loginUseCase: LoginAndMigrateUseCase,
    ): LoginUseCase

    @Binds
    internal abstract fun bindsDeleteBackupsUseCase(
        deleteBackupsUseCase: DeleteBackupsUseCaseImpl,
    ): DeleteBackupsUseCase
}
