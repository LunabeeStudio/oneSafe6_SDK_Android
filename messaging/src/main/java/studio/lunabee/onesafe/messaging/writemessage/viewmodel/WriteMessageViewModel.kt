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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.bubbles.domain.BubblesConstant
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.messaging.domain.repository.SentMessageRepository
import studio.lunabee.onesafe.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveSentMessageUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageUiState
import java.time.Instant
import java.util.Random
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getContactUseCase: GetContactUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val messageRepository: MessageRepository,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val channelRepository: MessageChannelRepository,
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val sentMessageRepository: SentMessageRepository,
    private val messageIdProvider: MessageIdProvider,
    private val contactRepository: ContactRepository,
    osAppSettings: OSAppSettings,
) : ViewModel() {

    private val _uiState: MutableStateFlow<WriteMessageUiState> = MutableStateFlow(WriteMessageUiState())
    val uiState: StateFlow<WriteMessageUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    val isMaterialYouSettingsEnabled: Flow<Boolean> = osAppSettings.materialYouSetting
    val isPreviewEnabled: StateFlow<Boolean> = osAppSettings.bubblesPreview.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true,
    )

    val contactId: StateFlow<UUID?> = savedStateHandle.getStateFlow(
        WriteMessageDestination.ContactIdArgs,
        savedStateHandle.get<String>(WriteMessageDestination.ContactIdArgs),
    ).map {
        it.let(UUID::fromString)
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        savedStateHandle.get<String>(WriteMessageDestination.ContactIdArgs)?.let(UUID::fromString),
    )

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
                contactId.value?.let { contactId ->
                    messageRepository.markMessagesAsRead(contactId)
                    contactRepository.updateContactConsultedAt(contactId, Instant.now())
                }
                pagingData.map { message ->
                    // TODO bubbles handle decrypt error
                    val sentAt = contactLocalDecryptUseCase(message.encSentAt, message.fromContactId, Instant::class).data!!
                    val plainContent = contactLocalDecryptUseCase(message.encContent, message.fromContactId, String::class).data!!
                    val channel = message.encChannel?.let { encChannel ->
                        contactLocalDecryptUseCase(encChannel, message.fromContactId, String::class).data!!
                    }
                    val text: LbcTextSpec
                    val type: ConversationUiData.MessageType
                    if (plainContent == BubblesConstant.FirstMessageData) {
                        text = LbcTextSpec.StringResource(R.string.bubbles_acceptedInvitation)
                        type = ConversationUiData.MessageType.Invitation
                    } else {
                        text = LbcTextSpec.Raw(plainContent)
                        type = ConversationUiData.MessageType.Message
                    }
                    ConversationUiData.Message(
                        id = message.id,
                        text = text,
                        direction = message.direction,
                        sendAt = sentAt,
                        channelName = channel,
                        type = type,
                    )
                }.insertSeparators { before, after ->
                    when {
                        before == null || before.wereSentOnSameDay(after) -> null
                        else -> ConversationUiData.DateHeader(before.sendAt)
                    }
                }
            }

    init {
        viewModelScope.launch {
            contactId.filterNotNull().flatMapLatest { contactId ->
                getContactUseCase(contactId).filterNotNull().distinctUntilChanged().map { encContact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(encContact.encName, encContact.id, String::class)
                    _uiState.value = _uiState.value.copy(
                        currentContact = UIBubblesContactInfo(
                            id = encContact.id,
                            nameProvider = if (decryptedNameResult is LBResult.Failure) {
                                ErrorNameProvider
                            } else {
                                OSNameProvider.fromName(
                                    name = decryptedNameResult.data,
                                    hasIcon = false,
                                )
                            },
                            conversationState = ConversationState.FullySetup,
                        ),
                        isUsingDeepLink = contactLocalDecryptUseCase(encContact.encIsUsingDeeplink, encContact.id, Boolean::class).data
                            ?: true,
                        isConversationReady = getConversationStateUseCase(contactId = contactId)
                            != ConversationState.WaitingForReply,
                    )
                }
            }.collect()
        }
    }

    fun onPlainMessageChange(value: String) {
        _uiState.value = _uiState.value.copy(
            plainMessage = value,
            encryptedPreview = if (isPreviewEnabled.value) generatePreview() else "",
        )
    }

    private suspend fun generateSendMessageData(): SendMessageData? {
        return contactId.value?.let {
            val result = getSendMessageDataUseCase(it)
            when (result) {
                is LBResult.Success -> result.successData
                is LBResult.Failure -> {
                    _dialogState.value = ErrorDialogState(
                        error = result.throwable as? OSError,
                        actions = listOf(
                            DialogAction.commonOk(::dismissDialog),
                        ),
                        dismiss = ::dismissDialog,
                    )
                    null
                }
            }
        }
    }

    suspend fun encryptAndSaveMessage(content: String): String? {
        onPlainMessageChange("")
        return generateSendMessageData()?.let { sendMessageData ->
            lastMessageChange = Instant.now()
            val contactId = contactId.value!!
            val messageId = messageIdProvider()
            val messageOrder = saveMessageUseCase(
                plainMessage = OSPlainMessage(
                    content = content,
                    recipientId = contactId,
                    sentAt = lastMessageChange,
                ),
                contactId = contactId,
                channel = channelRepository.channel,
                id = messageId,
            ).data
            messageOrder?.let {
                val messageToSend = encryptMessageUseCase(
                    content,
                    uiState.value.currentContact!!.id,
                    lastMessageChange,
                    sendMessageData,
                ).data
                messageToSend?.let {
                    saveSentMessageUseCase(
                        id = messageId,
                        messageString = messageToSend,
                        contactId = contactId,
                        createdAt = lastMessageChange,
                        order = messageOrder,
                    )
                }
                messageToSend
            }
        }
    }

    suspend fun getSentMessage(sentMessageId: UUID): String? {
        val encMessage = sentMessageRepository.getSentMessage(sentMessageId)
        val result = encMessage?.let { contactLocalDecryptUseCase(encMessage.encContent, contactId.value!!, String::class) }
        return when (result) {
            is LBResult.Success -> result.data
            else -> {
                displayTooOldMessageDialog()
                null
            }
        }
    }

    fun deleteMessage(messageId: UUID) {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_deleteMessage_message)
            override val title: LbcTextSpec = LbcTextSpec.StringResource(R.string.common_warning)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(
                DialogAction(
                    text = LbcTextSpec.StringResource(R.string.common_confirm),
                    type = DialogAction.Type.Dangerous,
                    onClick = {
                        dismissDialog()
                        viewModelScope.launch { messageRepository.deleteMessage(messageId) }
                    },
                ),
                DialogAction.commonCancel(::dismissDialog),
            )
        }
    }

    fun displayRemoveConversationDialog() {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_deleteDialog_message)
            override val title: LbcTextSpec = LbcTextSpec.StringResource(R.string.common_warning)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(
                DialogAction(
                    text = LbcTextSpec.StringResource(R.string.common_confirm),
                    type = DialogAction.Type.Dangerous,
                    onClick = {
                        dismissDialog()
                        viewModelScope.launch { messageRepository.deleteAllMessages(contactId.value!!) }
                    },
                ),
                DialogAction.commonCancel(::dismissDialog),
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    // Simply create a random byte array, and encode it to Base64
    private fun generatePreview(): String {
        return Base64.encode(ByteArray(128).apply { random.nextBytes(this) })
    }

    fun displayPreviewInfo() {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.writeMessageScreen_previewInfo_description)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(DialogAction.commonOk(::dismissDialog))
            override val title: LbcTextSpec = LbcTextSpec.StringResource(R.string.writeMessageScreen_previewInfo_title)
        }
    }

    private fun displayTooOldMessageDialog() {
        _dialogState.value = object : DialogState {
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(DialogAction.commonOk(::dismissDialog))
            override val title: LbcTextSpec = LbcTextSpec.StringResource(R.string.common_warning)
            override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_tooOldMessage)
        }
    }

    fun dismissDialog() {
        _dialogState.value = null
    }

    private companion object {
        private val random = Random()
    }
}
