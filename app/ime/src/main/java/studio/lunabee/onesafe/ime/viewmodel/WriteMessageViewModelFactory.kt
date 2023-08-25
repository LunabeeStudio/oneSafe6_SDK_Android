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
 * Created by Lunabee Studio / Date - 6/16/2023 - for the oneSafe6 SDK.
 * Last modified 6/16/23, 11:31 AM
 */

package studio.lunabee.onesafe.ime.viewmodel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import studio.lunabee.onesafe.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveSentMessageUseCase
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import javax.inject.Inject

class WriteMessageViewModelFactory @Inject constructor(
    private val getEncContactInfoUseCase: GetContactUseCase,
    private val decryptForContactUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val messageRepository: MessageRepository,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val channelRepository: MessageChannelRepository,
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val osAppSettings: OSAppSettings,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val sentMessageRepository: SentMessageRepository,
    private val messageIdProvider: MessageIdProvider,
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        @Suppress("UNCHECKED_CAST")
        return WriteMessageViewModel(
            savedStateHandle = handle,
            getContactUseCase = getEncContactInfoUseCase,
            contactLocalDecryptUseCase = decryptForContactUseCase,
            encryptMessageUseCase = encryptMessageUseCase,
            messageRepository = messageRepository,
            saveMessageUseCase = saveMessageUseCase,
            channelRepository = channelRepository,
            getSendMessageDataUseCase = getSendMessageDataUseCase,
            osAppSettings = osAppSettings,
            getConversationStateUseCase = getConversationStateUseCase,
            saveSentMessageUseCase = saveSentMessageUseCase,
            sentMessageRepository = sentMessageRepository,
            messageIdProvider = messageIdProvider,
        ) as T
    }
}
