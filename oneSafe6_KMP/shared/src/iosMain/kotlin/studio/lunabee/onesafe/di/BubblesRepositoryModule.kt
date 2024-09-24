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

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.repository.BubblesCryptoRepositoryImpl
import studio.lunabee.bubbles.repository.ContactKeyRepositoryImpl
import studio.lunabee.bubbles.repository.ContactRepositoryImpl

fun bubblesRepositoryModule(
    bubblesSafeRepository: BubblesSafeRepository,
): Module = module {
    single<ContactKeyRepository> { ContactKeyRepositoryImpl(get()) }
    single<BubblesCryptoRepository> { BubblesCryptoRepositoryImpl(get(), get(), get(), get(), get()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<BubblesSafeRepository> { bubblesSafeRepository }
}

class BubblesRepositories : KoinComponent {
    val contactKeyRepository: ContactKeyRepository by inject()
    val bubblesCryptoRepository: BubblesCryptoRepository by inject()
    val contactRepository: ContactRepository by inject()
    val bubblesSafeRepository: BubblesSafeRepository by inject()
}
