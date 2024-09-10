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

package studio.lunabee.bubbles.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import studio.lunabee.messaging.domain.repository.ConversationRepository
import studio.lunabee.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.messaging.domain.repository.MessageOrderRepository
import studio.lunabee.messaging.domain.repository.MessagePagingRepository
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.messaging.repository.ConversationRepositoryImpl
import studio.lunabee.messaging.repository.EnqueuedMessageRepositoryImpl
import studio.lunabee.messaging.repository.HandShakeDataRepositoryImpl
import studio.lunabee.messaging.repository.MessageChannelRepositoryImpl
import studio.lunabee.messaging.repository.MessageOrderRepositoryImpl
import studio.lunabee.messaging.repository.MessagePagingRepositoryImpl
import studio.lunabee.messaging.repository.MessageRepositoryImpl
import studio.lunabee.messaging.repository.MessagingCryptoRepositoryImpl
import studio.lunabee.messaging.repository.SentMessageRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface MessagingRepositoryModule {
    @Binds
    fun bindConversationRepository(conversationRepositoryImpl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    fun bindEnqueuedMessageRepository(enqueuedMessageRepositoryImpl: EnqueuedMessageRepositoryImpl): EnqueuedMessageRepository

    @Binds
    fun bindHandShakeDataRepository(handShakeDataRepositoryImpl: HandShakeDataRepositoryImpl): HandShakeDataRepository

    @Binds
    fun bindMessageChannelRepository(messageChannelRepositoryImpl: MessageChannelRepositoryImpl): MessageChannelRepository

    @Binds
    fun bindMessageOrderRepository(messageOrderRepositoryImpl: MessageOrderRepositoryImpl): MessageOrderRepository

    @Binds
    fun bindMessageRepository(messageRepositoryImpl: MessageRepositoryImpl): MessageRepository

    @Binds
    fun bindSentMessageRepository(sentMessageRepositoryImpl: SentMessageRepositoryImpl): SentMessageRepository

    @Binds
    fun bindMessagePagingRepository(messagePagingRepositoryImpl: MessagePagingRepositoryImpl): MessagePagingRepository

    @Binds
    fun bindsMessageCryptoRepository(messageCryptoRepositoryImpl: MessagingCryptoRepositoryImpl): MessagingCryptoRepository
}
