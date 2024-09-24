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
import studio.lunabee.messaging.domain.repository.ConversationRepository
import studio.lunabee.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.messaging.domain.repository.MessageOrderRepository
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.messaging.domain.repository.MessagingSettingsRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.messaging.repository.ConversationRepositoryImpl
import studio.lunabee.messaging.repository.EnqueuedMessageRepositoryImpl
import studio.lunabee.messaging.repository.HandShakeDataRepositoryImpl
import studio.lunabee.messaging.repository.MessageChannelRepositoryImpl
import studio.lunabee.messaging.repository.MessageOrderRepositoryImpl
import studio.lunabee.messaging.repository.MessageRepositoryImpl
import studio.lunabee.messaging.repository.MessagingCryptoRepositoryImpl
import studio.lunabee.messaging.repository.SentMessageRepositoryImpl

fun messagingRepositoryModule(
    messagingSettingsRepository: MessagingSettingsRepository,
): Module = module {
    single<ConversationRepository> { ConversationRepositoryImpl(get()) }
    single<EnqueuedMessageRepository> { EnqueuedMessageRepositoryImpl(get()) }
    single<HandShakeDataRepository> { HandShakeDataRepositoryImpl(get()) }
    single<MessageChannelRepository> { MessageChannelRepositoryImpl() }
    single<MessageOrderRepository> { MessageOrderRepositoryImpl(get()) }
    single<MessageRepository> { MessageRepositoryImpl(get()) }
    single<MessagingSettingsRepository> { messagingSettingsRepository }
    single<SentMessageRepository> { SentMessageRepositoryImpl(get()) }
    single<MessagingCryptoRepository> { MessagingCryptoRepositoryImpl(get(), get(), get(), get()) }
}

class MessagingRepositories : KoinComponent {
    val conversationRepository: ConversationRepository by inject()
    val enqueuedMessageRepository: EnqueuedMessageRepository by inject()
    val handShakeDataRepository: HandShakeDataRepository by inject()
    val messageChannelRepository: MessageChannelRepository by inject()
    val messageOrderRepository: MessageOrderRepository by inject()
    val messageRepository: MessageRepository by inject()
    val messagingSettingsRepository: MessagingSettingsRepository by inject()
    val sentMessageRepository: SentMessageRepository by inject()
}
