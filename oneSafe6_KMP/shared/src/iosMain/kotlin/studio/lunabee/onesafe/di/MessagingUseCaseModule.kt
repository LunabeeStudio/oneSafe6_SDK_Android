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
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import studio.lunabee.messaging.domain.MessageOrderCalculator
import studio.lunabee.messaging.domain.usecase.AcceptInvitationUseCase
import studio.lunabee.messaging.domain.usecase.CreateInvitationUseCase
import studio.lunabee.messaging.domain.usecase.CryptoHandShakeDataUseCase
import studio.lunabee.messaging.domain.usecase.DecryptIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.DecryptSafeMessageUseCase
import studio.lunabee.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.messaging.domain.usecase.EnqueueMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.messaging.domain.usecase.GetHandShakeDataUseCase
import studio.lunabee.messaging.domain.usecase.GetInvitationMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetInvitationResponseMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.messaging.domain.usecase.HandleIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.InsertHandShakeDataUseCase
import studio.lunabee.messaging.domain.usecase.ManageIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.ProcessMessageQueueUseCase
import studio.lunabee.messaging.domain.usecase.RemoveOldSentMessagesUseCase
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.messaging.domain.usecase.SaveSentMessageUseCase

val messagingUseCaseModule = module {
    singleOf(::AcceptInvitationUseCase)
    singleOf(::CreateInvitationUseCase)
    singleOf(::CryptoHandShakeDataUseCase)
    singleOf(::DecryptIncomingMessageUseCase)
    singleOf(::DecryptSafeMessageUseCase)
    singleOf(::EncryptMessageUseCase)
    singleOf(::EnqueueMessageUseCase)
    singleOf(::GetConversationStateUseCase)
    singleOf(::GetHandShakeDataUseCase)
    singleOf(::GetInvitationMessageUseCase)
    singleOf(::GetInvitationResponseMessageUseCase)
    singleOf(::GetSendMessageDataUseCase)
    singleOf(::HandleIncomingMessageUseCase)
    singleOf(::InsertHandShakeDataUseCase)
    singleOf(::ManageIncomingMessageUseCase)
    singleOf(::ProcessMessageQueueUseCase)
    singleOf(::RemoveOldSentMessagesUseCase)
    singleOf(::SaveMessageUseCase)
    singleOf(::SaveSentMessageUseCase)
    singleOf(::MessageOrderCalculator)
}

class MessagingUseCases : KoinComponent {
    val acceptInvitationUseCase: AcceptInvitationUseCase by inject()
    val createInvitationUseCase: CreateInvitationUseCase by inject()
    val cryptoHandShakeDataUseCase: CryptoHandShakeDataUseCase by inject()
    val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase by inject()
    val decryptSafeMessageUseCase: DecryptSafeMessageUseCase by inject()
    val encryptMessageUseCase: EncryptMessageUseCase by inject()
    val enqueueMessageUseCase: EnqueueMessageUseCase by inject()
    val getConversationStateUseCase: GetConversationStateUseCase by inject()
    val getHandShakeDataUseCase: GetHandShakeDataUseCase by inject()
    val getInvitationMessageUseCase: GetInvitationMessageUseCase by inject()
    val getInvitationResponseMessageUseCase: GetInvitationResponseMessageUseCase by inject()
    val getSendMessageDataUseCase: GetSendMessageDataUseCase by inject()
    val handleIncomingMessageUseCase: HandleIncomingMessageUseCase by inject()
    val insertHandShakeDataUseCase: InsertHandShakeDataUseCase by inject()
    val manageIncomingMessageUseCase: ManageIncomingMessageUseCase by inject()
    val processMessageQueueUseCase: ProcessMessageQueueUseCase by inject()
    val removeOldSentMessagesUseCase: RemoveOldSentMessagesUseCase by inject()
    val saveMessageUseCase: SaveMessageUseCase by inject()
    val saveSentMessageUseCase: SaveSentMessageUseCase by inject()
    val messageOrderCalculator: MessageOrderCalculator by inject()
}
