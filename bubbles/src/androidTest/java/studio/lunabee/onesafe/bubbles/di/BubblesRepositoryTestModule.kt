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
 * Created by Lunabee Studio / Date - 5/23/2023 - for the oneSafe6 SDK.
 * Last modified 5/23/23, 9:21 AM
 */

package studio.lunabee.onesafe.bubbles.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesContactRepository
import studio.lunabee.onesafe.bubbles.repository.BubblesContactRepositoryImpl
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.VersionName

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BubblesRepositoryModule::class],
)
interface BubblesRepositoryTestModule {
    @Binds
    fun bindBubblesContactRepository(bubblesContactRepository: BubblesContactRepositoryImpl): BubblesContactRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FrameWorkForBubblesTestModule {
    @Provides
    @BuildNumber
    @Suppress("FunctionOnlyReturningConstant")
    fun provideBuildNumber(): Int = 9999

    @Provides
    @VersionName
    @Suppress("FunctionOnlyReturningConstant")
    fun provideVersionName(): String = "1.11.0"
}
