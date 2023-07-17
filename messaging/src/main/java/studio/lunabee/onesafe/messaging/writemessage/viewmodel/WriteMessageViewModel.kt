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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 2:48 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageUiState
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getContactUseCase: GetContactUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val messageRepository: MessageRepository,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val channelRepository: MessageChannelRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<WriteMessageUiState> = MutableStateFlow(WriteMessageUiState())
    val uiState: StateFlow<WriteMessageUiState> = _uiState.asStateFlow()

    private var lastMessageChange = Instant.now()

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversation: Flow<PagingData<ConversationUiData>> =
        savedStateHandle
            .getStateFlow<String?>(WriteMessageDestination.ContactIdArgs, null)
            .map { it?.let { UUID.fromString(it) } }
            .filterNotNull()
            .flatMapLatest { contactId ->
                messageRepository.getAllPaged(
                    config = PagingConfig(pageSize = 15, jumpThreshold = 30),
                    contactId = contactId,
                )
            }.map { pagingData ->
                pagingData.map { message ->
                    // TODO handle decrypt error
                    val sentAt = contactLocalDecryptUseCase(message.encSentAt, message.fromContactId, Instant::class).data!!
                    val content = contactLocalDecryptUseCase(message.encContent, message.fromContactId, String::class).data!!
                    val channel = message.encChannel?.let { encChannel ->
                        contactLocalDecryptUseCase(encChannel, message.fromContactId, String::class).data!!
                    }
                    ConversationUiData.PlainMessageData(
                        id = "contact_${message.id}",
                        text = content,
                        direction = message.direction,
                        sendAt = sentAt,
                        channelName = channel,
                    )
                }.insertSeparators { before, after ->
                    when {
                        before == null || before.wereSentOnSameDay(after) -> null
                        else -> ConversationUiData.DateHeader(before.sendAt)
                    }
                }
            }

    init {
        savedStateHandle
            .getStateFlow<String?>(WriteMessageDestination.ContactIdArgs, null)
            .onEach { contactId ->
                getContactUseCase(UUID.fromString(contactId!!))?.let { encContact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(encContact.encName, encContact.id, String::class)
                    _uiState.value = _uiState.value.copy(
                        currentContact = BubblesContactInfo(
                            id = encContact.id,
                            nameProvider = if (decryptedNameResult is LBResult.Failure) {
                                ErrorNameProvider
                            } else {
                                OSNameProvider.fromName(
                                    name = decryptedNameResult.data,
                                    hasIcon = false,
                                )
                            },
                        ),
                    )
                    encryptPlainMessage()
                }
            }.launchIn(viewModelScope)
    }

    fun onPlainMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(plainMessage = value)
        encryptPlainMessage()
    }

    private fun encryptPlainMessage() {
        viewModelScope.launch {
            val plainMessage = uiState.value.plainMessage
            lastMessageChange = Instant.now()
            val encryptResult = encryptMessageUseCase(plainMessage, uiState.value.currentContact!!.id, lastMessageChange)
            _uiState.value = _uiState.value.copy(
                encryptedMessage = if (encryptResult is LBResult.Success) {
                    encryptResult.successData
                } else {
                    ""
                },
            )
        }
    }

    fun saveMessage(content: String, contactId: UUID, context: Context) {
        viewModelScope.launch {
            saveMessageUseCase(
                plainMessage = content,
                sentAt = lastMessageChange,
                contactId = contactId,
                recipientId = contactId,
                channel = channelRepository.channel ?: context.getString(R.string.oneSafeK_channel_oneSafeSharing),
            )
        }
    }
}
