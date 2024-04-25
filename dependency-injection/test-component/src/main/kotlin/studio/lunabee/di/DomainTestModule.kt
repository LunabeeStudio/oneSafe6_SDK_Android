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
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.threeten.extra.MutableClock
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.storage.DoubleRatchetLocalDatasource
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.test.IncrementalIdProvider
import java.io.File
import java.time.Clock
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DomainModule::class],
)
object DomainTestModule {
    @Provides
    @Singleton
    fun provideItemIdProvider(): ItemIdProvider = IncrementalIdProvider()

    @Provides
    @Singleton
    fun provideFieldIdProvider(): FieldIdProvider = IncrementalIdProvider()

    @Provides
    @Singleton
    fun provideIconIdProvider(): IconIdProvider = IncrementalIdProvider()

    @Provides
    @Singleton
    fun provideFileIdProvider(): FileIdProvider = IncrementalIdProvider()

    @Provides
    @Singleton
    fun provideMessageIdProvider(): MessageIdProvider = IncrementalIdProvider()

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
    fun providesClock(): Clock = MutableClock.epochUTC()

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
}
