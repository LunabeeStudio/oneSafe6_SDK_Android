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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import studio.lunabee.onesafe.domain.common.IconIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.common.UuidProvider
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.SetIconUseCase
import studio.lunabee.onesafe.domain.utils.SafeItemBuilder
import studio.lunabee.onesafe.domain.utils.SafeItemBuilderImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
object DomainModule {
    @Provides
    fun provideItemIdProvider(): ItemIdProvider = UuidProvider()

    @Provides
    fun provideIconIdProvider(): IconIdProvider = UuidProvider()

    @Provides
    @FileDispatcher
    fun providesFileDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun provideSafeItemBuilder(cryptoRepository: MainCryptoRepository, setIconUseCase: SetIconUseCase): SafeItemBuilder =
        SafeItemBuilderImpl(cryptoRepository = cryptoRepository, setIconUseCase = setIconUseCase)
}
