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
import com.lunabee.lbloading.LoadingManager
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.messaging.domain.repository.MessagePagingRepository
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.messaging.domain.usecase.DecryptSafeMessageUseCase
import studio.lunabee.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.messaging.domain.usecase.SaveSentMessageUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.messaging.usecase.CreateSingleEntryArchiveUseCase
import studio.lunabee.onesafe.messaging.usecase.DeleteBubblesArchiveUseCase
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import java.time.Clock
import javax.inject.Inject

class WriteMessageViewModelFactory @Inject constructor(
    private val getEncContactInfoUseCase: GetContactUseCase,
    private val decryptForContactUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val messageRepository: MessageRepository,
    private val channelRepository: MessageChannelRepository,
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val sentMessageRepository: SentMessageRepository,
    private val contactRepository: ContactRepository,
    private val clock: Clock,
    private val decryptSafeMessageUseCase: DecryptSafeMessageUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val messagePagingRepository: MessagePagingRepository,
    private val loadingManager: LoadingManager,
    private val createSingleEntryArchiveUseCase: CreateSingleEntryArchiveUseCase,
    private val deleteBubblesArchiveUseCase: DeleteBubblesArchiveUseCase,
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        @Suppress("UNCHECKED_CAST")
        return WriteMessageViewModel(
            savedStateHandle = handle,
            getContactUseCase = getEncContactInfoUseCase,
            contactLocalDecryptUseCase = decryptForContactUseCase,
            encryptMessageUseCase = encryptMessageUseCase,
            messageRepository = messageRepository,
            channelRepository = channelRepository,
            getSendMessageDataUseCase = getSendMessageDataUseCase,
            getConversationStateUseCase = getConversationStateUseCase,
            saveSentMessageUseCase = saveSentMessageUseCase,
            sentMessageRepository = sentMessageRepository,
            contactRepository = contactRepository,
            clock = clock,
            decryptSafeMessageUseCase = decryptSafeMessageUseCase,
            isSafeReadyUseCase = isSafeReadyUseCase,
            getAppSettingUseCase = getAppSettingUseCase,
            messagePagingRepository = messagePagingRepository,
            loadingManager = loadingManager,
            createSingleEntryArchiveUseCase = createSingleEntryArchiveUseCase,
            deleteBubblesArchiveUseCase = deleteBubblesArchiveUseCase,
        ) as T
    }
}
