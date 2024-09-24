/*
 * Copyright (c) 2024 Lunabee Studio
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
 */

package studio.lunabee.onesafe.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.repository.BubblesCryptoRepositoryImpl
import studio.lunabee.bubbles.repository.ContactKeyRepositoryImpl
import studio.lunabee.bubbles.repository.ContactRepositoryImpl
import studio.lunabee.bubbles.repository.DoubleRatchetKeyRepositoryImpl
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface BubblesRepositoryModule {
    @Binds
    fun bindsContactKeyRepository(contactKeyRepositoryImpl: ContactKeyRepositoryImpl): ContactKeyRepository

    @Binds
    fun bindsContactRepository(contactRepositoryImpl: ContactRepositoryImpl): ContactRepository

    @Binds
    fun bindsBubblesCryptoRepository(bubblesCryptoRepositoryImpl: BubblesCryptoRepositoryImpl): BubblesCryptoRepository

    @Binds
    fun bindsDoubleRatchetKeyRepository(doubleRatchetKeyRepositoryImpl: DoubleRatchetKeyRepositoryImpl): DoubleRatchetKeyRepository
}
