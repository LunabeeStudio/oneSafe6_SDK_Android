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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 11:26 AM
 */

package studio.lunabee.onesafe.bubbles.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.bubbles.local.BubblesDatabase
import studio.lunabee.onesafe.bubbles.local.datasource.BubblesContactLocalDataSourceImpl
import studio.lunabee.onesafe.bubbles.repository.datasource.BubblesContactLocalDataSource
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BubblesDatabaseModule::class],
)
object BubblesDatabaseTestModule {
    @Provides
    @Singleton
    fun provideBubblesDatabase(@ApplicationContext appContext: Context): BubblesDatabase {
        return Room.inMemoryDatabaseBuilder(appContext, BubblesDatabase::class.java).build()
    }
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BubblesStorageModule::class],
)
interface BubblesStorageTestModule {
    @Binds
    fun bindsBubblesContactLocalDataSource(bubblesContactLocalDataSource: BubblesContactLocalDataSourceImpl): BubblesContactLocalDataSource
}
