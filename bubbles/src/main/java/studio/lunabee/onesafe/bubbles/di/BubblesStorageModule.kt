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
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.lunabee.onesafe.bubbles.local.dao.BubblesContactDao
import studio.lunabee.onesafe.bubbles.local.BubblesDatabase
import studio.lunabee.onesafe.bubbles.local.datasource.BubblesContactLocalDataSourceImpl
import studio.lunabee.onesafe.bubbles.repository.datasource.BubblesContactLocalDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BubblesDatabaseModule {
    @Provides
    @Singleton
    fun provideBubblesDatabase(@ApplicationContext appContext: Context): BubblesDatabase {
        return Room.databaseBuilder(
            appContext,
            BubblesDatabase::class.java,
            "58252457-42de-4313-a007-1b7c80445bf6",
        ).build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object BubblesDatabaseDaoModule {

    @Provides
    fun provideBubblesContactDao(bubblesDatabase: BubblesDatabase): BubblesContactDao {
        return bubblesDatabase.bubblesContactDao()
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface BubblesStorageModule {
    @Binds
    fun bindsBubblesContactLocalDataSource(bubblesContactLocalDataSource: BubblesContactLocalDataSourceImpl): BubblesContactLocalDataSource
}
