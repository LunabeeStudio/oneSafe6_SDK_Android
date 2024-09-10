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

import org.koin.dsl.module
import studio.lunabee.messaging.repository.datasource.ConversationLocalDatasource
import studio.lunabee.messaging.repository.datasource.DoubleRatchetKeyLocalDatasource
import studio.lunabee.messaging.repository.datasource.EnqueuedMessageLocalDataSource
import studio.lunabee.messaging.repository.datasource.HandShakeDataLocalDatasource
import studio.lunabee.messaging.repository.datasource.MessageLocalDataSource
import studio.lunabee.messaging.repository.datasource.MessageQueueLocalDatasource
import studio.lunabee.messaging.repository.datasource.SentMessageLocalDatasource

@Suppress("LongParameterList")
fun messagingDatasourceModule(
    sentMessageLocalDatasource: SentMessageLocalDatasource,
    messageLocalDataSource: MessageLocalDataSource,
    handShakeDataLocalDatasource: HandShakeDataLocalDatasource,
    enqueuedMessageLocalDataSource: EnqueuedMessageLocalDataSource,
    conversationLocalDatasource: ConversationLocalDatasource,
    doubleRatchetKeyLocalDatasource: DoubleRatchetKeyLocalDatasource,
    messageQueueLocalDatasource: MessageQueueLocalDatasource,
) = module {
    single<SentMessageLocalDatasource> { sentMessageLocalDatasource }
    single<MessageLocalDataSource> { messageLocalDataSource }
    single<HandShakeDataLocalDatasource> { handShakeDataLocalDatasource }
    single<EnqueuedMessageLocalDataSource> { enqueuedMessageLocalDataSource }
    single<ConversationLocalDatasource> { conversationLocalDatasource }
    single<DoubleRatchetKeyLocalDatasource> { doubleRatchetKeyLocalDatasource }
    single<MessageQueueLocalDatasource> { messageQueueLocalDatasource }
}
