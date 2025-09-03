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

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.messaging.domain.model.ConversationState
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.messaging.domain.model.PlainMessageData
import studio.lunabee.messaging.domain.model.SentMessage
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.messaging.domain.repository.MessagePagingRepository
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.messaging.domain.usecase.DecryptSafeMessageUseCase
import studio.lunabee.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.messaging.domain.usecase.GetResetMessageUseCase
import studio.lunabee.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.messaging.domain.usecase.SaveSentMessageUseCase
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.RemovedNameProvider
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.messaging.usecase.CreateBubblesMessageArchiveUseCase
import studio.lunabee.onesafe.messaging.usecase.DeleteBubblesArchiveUseCase
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.model.BubblesWritingMessage
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.model.SaveMessageConfirmationDialogState
import studio.lunabee.onesafe.messaging.writemessage.model.SaveMessageConfirmationSnackbarState
import studio.lunabee.onesafe.messaging.writemessage.model.SentMessageData
import studio.lunabee.onesafe.messaging.writemessage.model.WriteContactInfo
import studio.lunabee.onesafe.messaging.writemessage.model.WriteConversationState
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageUiState
import studio.lunabee.onesafe.ui.extensions.toColor
import java.io.File
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle,
    private val getContactUseCase: GetContactUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val messageRepository: MessageRepository,
    private val messagePagingRepository: MessagePagingRepository,
    private val channelRepository: MessageChannelRepository,
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val sentMessageRepository: SentMessageRepository,
    private val contactRepository: ContactRepository,
    private val clock: Clock,
    private val decryptSafeMessageUseCase: DecryptSafeMessageUseCase,
    private val loadingManager: LoadingManager,
    private val deleteBubblesArchiveUseCase: DeleteBubblesArchiveUseCase,
    private val createBubblesMessageArchiveUseCase: CreateBubblesMessageArchiveUseCase,
    private val secureGetItemUseCase: SecureGetItemUseCase,
    private val itemDecryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val getResetMessageUseCase: GetResetMessageUseCase,
    private val setAppVisitUseCase: SetAppVisitUseCase,
    private val getAppVisitUseCase: GetAppVisitUseCase,
    isSafeReadyUseCase: IsSafeReadyUseCase,
    getAppSettingUseCase: GetAppSettingUseCase,
) : ViewModel() {

    private val _snackbarState: MutableStateFlow<SnackbarState?> = MutableStateFlow(null)
    val snackbarState: StateFlow<SnackbarState?> = _snackbarState.asStateFlow()

    init {
        savedStateHandle.get<String>(WriteMessageDestination.ErrorArg)?.let {
            val error = DecryptResult.Error.valueOf(it).error
            _snackbarState.value = ErrorSnackbarState(error) {}
            savedStateHandle[WriteMessageDestination.ErrorArg] = null
        }
    }

    val contactId: StateFlow<DoubleRatchetUUID?> = savedStateHandle.getStateFlow(
        WriteMessageDestination.ContactIdArg,
        savedStateHandle.get<String>(WriteMessageDestination.ContactIdArg),
    ).map {
        it?.let { DoubleRatchetUUID.fromString(it) }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        savedStateHandle.get<String>(WriteMessageDestination.ContactIdArg)?.let { DoubleRatchetUUID.fromString(it) },
    )

    private val contactFlow = savedStateHandle.getStateFlow(
        WriteMessageDestination.ContactIdArg,
        savedStateHandle.get<String>(WriteMessageDestination.ContactIdArg),
    ).flatMapLatest { contactId ->
        val uuid = DoubleRatchetUUID.fromString(contactId.orEmpty())
        this.contactId.value?.let { getContactUseCase.flow(uuid).distinctUntilChanged() } ?: flowOf(null)
    }

    private val writeContactInfoFlow = combine(
        contactFlow,
        isSafeReadyUseCase.flow(),
    ) { encContact, isCryptoDataReadyInMemory ->
        if (isCryptoDataReadyInMemory) {
            encContact?.let {
                val decryptedNameResult = contactLocalDecryptUseCase(encContact.encName, encContact.id, String::class)
                val sharingMode = contactLocalDecryptUseCase(encContact.encSharingMode, encContact.id, MessageSharingMode::class).data
                    ?: MessageSharingMode.Deeplink
                val nameProvider = if (decryptedNameResult is LBResult.Failure) {
                    ErrorNameProvider
                } else {
                    OSNameProvider.fromName(
                        name = decryptedNameResult.data,
                        hasIcon = false,
                    )
                }
                val conversationId = encContact.id
                val conversationState: LBResult<ConversationState> = getConversationStateUseCase(id = conversationId)
                when (conversationState) {
                    is LBResult.Failure -> WriteContactInfo(
                        id = conversationId,
                        nameProvider = nameProvider,
                        messageSharingMode = sharingMode,
                        conversationState = WriteConversationState.Ready, // default to ready to show the default UI
                        isCorrupted = true,
                    )
                    is LBResult.Success -> {
                        WriteContactInfo(
                            id = conversationId,
                            nameProvider = nameProvider,
                            messageSharingMode = sharingMode,
                            conversationState = when (conversationState.successData) {
                                ConversationState.Reset -> WriteConversationState.Reset
                                ConversationState.WaitingForReply -> WriteConversationState.WaitingForReply
                                ConversationState.Running,
                                ConversationState.FullySetup,
                                ConversationState.WaitingForFirstMessage,
                                -> WriteConversationState.Ready
                            },
                            isCorrupted = false,
                        )
                    }
                }
            }
        } else {
            null
        }
    }

    private val plainMessageFlow: MutableStateFlow<TextFieldValue> = MutableStateFlow(TextFieldValue())
    private val bubblesWritingMessageFlow: Flow<BubblesWritingMessage> = combine(
        plainMessageFlow,
        getAppSettingUseCase.bubblesPreview(),
    ) { plainMessage, isPreviewEnabled ->
        val preview = when {
            !isPreviewEnabled -> null
            plainMessage.text.isEmpty() -> ""
            plainMessage.text == (uiState.value as? WriteMessageUiState.Data)?.message?.plainMessage?.text ->
                (uiState.value as WriteMessageUiState.Data).message.preview
            else -> generatePreview()
        }
        BubblesWritingMessage(plainMessage = plainMessage, preview = preview)
    }

    val uiState: StateFlow<WriteMessageUiState> = combine(
        writeContactInfoFlow,
        bubblesWritingMessageFlow,
        isSafeReadyUseCase.flow(),
    ) { writeContactInfo, bubblesWritingMessage, isCryptoDataReadyInMemory ->
        if (isCryptoDataReadyInMemory && writeContactInfo != null) {
            WriteMessageUiState.Data(
                contactId = writeContactInfo.id,
                nameProvider = writeContactInfo.nameProvider,
                messageSharingMode = writeContactInfo.messageSharingMode,
                conversationState = writeContactInfo.conversationState,
                message = bubblesWritingMessage,
                isCorrupted = writeContactInfo.isCorrupted,
            )
        } else {
            WriteMessageUiState.Initializing
        }
    }.stateIn(
        viewModelScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        WriteMessageUiState.Initializing,
    )

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private var lastMessageChange = Instant.now(clock)

    fun resetSnackbarState() {
        _snackbarState.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversation: Flow<PagingData<ConversationUiData>> =
        savedStateHandle
            .getStateFlow<String?>(WriteMessageDestination.ContactIdArg, null)
            .map { it?.let { DoubleRatchetUUID.fromString(it) } }
            .filterNotNull()
            .flatMapLatest { contactId ->
                messagePagingRepository.getAllPaged(
                    config = PagingConfig(pageSize = 15, jumpThreshold = 30),
                    contactId = contactId,
                )
            }
            .map { pagingData ->
                contactId.value?.let { contactId ->
                    messageRepository.markMessagesAsRead(contactId)
                    contactRepository.updateContactConsultedAt(contactId, Instant.now(clock).toKotlinInstant())
                }
                pagingData.map { message ->
                    val plainMessageData: PlainMessageData = decryptSafeMessageUseCase.message(message)
                    when (plainMessageData) {
                        is PlainMessageData.AcceptedInvitation -> {
                            ConversationUiData.Message.Text(
                                id = message.id,
                                text = LbcTextSpec.StringResource(OSString.bubbles_acceptedInvitation),
                                direction = message.direction,
                                date = plainMessageData.sentAt.data?.toJavaInstant(),
                                channelName = plainMessageData.channel?.data,
                                type = ConversationUiData.MessageType.Invitation,
                                hasCorruptedData = plainMessageData.hasCorruptedData,
                            )
                        }
                        is PlainMessageData.Default -> {
                            val text = when (val plainContent: LBResult<String> = plainMessageData.content) {
                                is LBResult.Failure -> {
                                    LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_corruptedMessage)
                                }
                                is LBResult.Success -> {
                                    LbcTextSpec.Raw(plainContent.successData)
                                }
                            }
                            ConversationUiData.Message.Text(
                                id = message.id,
                                text = text,
                                direction = message.direction,
                                date = plainMessageData.sentAt.data?.toJavaInstant(),
                                channelName = plainMessageData.channel?.data,
                                type = ConversationUiData.MessageType.Message,
                                hasCorruptedData = plainMessageData.hasCorruptedData,
                            )
                        }
                        is PlainMessageData.SafeItem -> {
                            val item = plainMessageData.itemId?.uuid?.let { secureGetItemUseCase.withIdentifier(it).firstOrNull() }
                            val name = item?.encName?.let { itemDecryptUseCase(it, item.id, String::class) }
                            val identifier = item?.encIdentifier?.let { itemDecryptUseCase(it, item.id, String::class) }
                            val icon = item?.iconId?.let { getIconUseCase(it, item.id) }
                            val color = item?.encColor?.let { itemDecryptUseCase(it, item.id, String::class) }?.data?.toColor()
                            val itemNameProvider = when {
                                item == null -> RemovedNameProvider
                                name is LBResult.Failure -> ErrorNameProvider
                                else -> OSNameProvider.fromName(name = name?.data, hasIcon = icon?.data != null)
                            }
                            val illustration = OSItemIllustrationHelper.get(itemNameProvider, icon?.data, color)
                            ConversationUiData.Message.SafeItem(
                                id = message.id,
                                direction = message.direction,
                                date = plainMessageData.sentAt.data?.toJavaInstant(),
                                channelName = plainMessageData.channel?.data,
                                icon = illustration,
                                name = itemNameProvider,
                                identifier = identifier?.data?.let(LbcTextSpec::Raw),
                                itemId = item?.id,
                            )
                        }
                        is PlainMessageData.ResetConversation -> ConversationUiData.ResetConversation(
                            date = plainMessageData.sentAt.data?.toJavaInstant(),
                        )
                    }
                }.insertSeparators { before, after ->
                    val beforeSendAt = before?.date
                    when {
                        beforeSendAt == null || before.wereSentOnSameDay(after) -> null
                        else -> ConversationUiData.DateHeader(beforeSendAt)
                    }
                }
            }

    fun onPlainMessageChange(value: TextFieldValue) {
        plainMessageFlow.value = value
    }

    suspend fun getSentMessageData(content: String): SentMessageData? {
        val contactId = contactId.value!!
        val messageDataRes: LBResult<SendMessageData> = getSendMessageDataUseCase(contactId)
        return when (messageDataRes) {
            is LBResult.Success -> {
                getSentMessageData(content, messageDataRes.successData, contactId)
            }
            is LBResult.Failure -> {
                displayErrorDialog(messageDataRes.throwable)
                null
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun getSentMessageData(
        content: String,
        messageData: SendMessageData,
        contactId: DoubleRatchetUUID,
    ): SentMessageData? {
        lastMessageChange = Instant.now(clock)
        val encMessageRes: LBResult<ByteArray> = encryptMessageUseCase(
            plainMessage = content,
            contactId = (uiState.value as WriteMessageUiState.Data).contactId,
            sentAt = lastMessageChange.toKotlinInstant(),
            sendMessageData = messageData,
        )
        return when (encMessageRes) {
            is LBResult.Failure -> {
                displayErrorDialog(encMessageRes.throwable)
                null
            }
            is LBResult.Success -> {
                SentMessageData(
                    encMessage = Base64.encode(encMessageRes.successData),
                    contactId = contactId,
                    createdAt = lastMessageChange,
                    plainMessage = content,
                )
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun saveEncryptedMessage(sentMessageData: SentMessageData) {
        viewModelScope.launch {
            val result: LBResult<SentMessage?> = saveSentMessageUseCase(
                plainMessage = SharedMessage(
                    content = sentMessageData.plainMessage,
                    recipientId = sentMessageData.contactId,
                    date = lastMessageChange.toKotlinInstant(),
                ),
                messageString = Base64.decode(sentMessageData.encMessage),
                contactId = sentMessageData.contactId,
                createdAt = sentMessageData.createdAt.toKotlinInstant(),
                channel = channelRepository.channel,
            )
            when (result) {
                is LBResult.Failure -> {
                    displayErrorDialog(result.throwable)
                }
                is LBResult.Success -> {
                    onPlainMessageChange(TextFieldValue())
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getSentMessage(sentMessageId: DoubleRatchetUUID): String? {
        val encMessage = sentMessageRepository.getSentMessage(sentMessageId)
        val result = encMessage?.let { contactLocalDecryptUseCase(encMessage.encContent, contactId.value!!, ByteArray::class) }
        return when (result) {
            is LBResult.Success -> result.data?.let { Base64.encode(it) }
            else -> {
                displayTooOldMessageDialog()
                null
            }
        }
    }

    fun deleteMessage(messageId: DoubleRatchetUUID) {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_deleteMessage_message)
            override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(
                DialogAction.commonCancel(::dismissDialog),
                DialogAction(
                    text = LbcTextSpec.StringResource(OSString.common_confirm),
                    type = DialogAction.Type.Dangerous,
                    onClick = {
                        dismissDialog()
                        viewModelScope.launch { messageRepository.deleteMessage(messageId) }
                    },
                ),
            )
            override val customContent:
                @Composable
                (() -> Unit)? = null
        }
    }

    fun displayRemoveConversationDialog() {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_deleteDialog_message)
            override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(
                DialogAction.commonCancel(::dismissDialog),
                DialogAction(
                    text = LbcTextSpec.StringResource(OSString.common_confirm),
                    type = DialogAction.Type.Dangerous,
                    onClick = {
                        dismissDialog()
                        viewModelScope.launch { messageRepository.deleteAllMessages(contactId.value!!) }
                    },
                ),
            )
            override val customContent:
                @Composable
                (() -> Unit)? = null
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    // Simply create a random byte array, and encode it to Base64
    private fun generatePreview(): String {
        return Base64.encode(Random.nextBytes(128))
    }

    fun displayPreviewInfo() {
        _dialogState.value = object : DialogState {
            override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.writeMessageScreen_previewInfo_description)
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(DialogAction.commonOk(::dismissDialog))
            override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.writeMessageScreen_previewInfo_title)
            override val customContent:
                @Composable
                (() -> Unit)? = null
        }
    }

    private fun displayTooOldMessageDialog() {
        _dialogState.value = object : DialogState {
            override val dismiss: () -> Unit = ::dismissDialog
            override val actions: List<DialogAction> = listOf(DialogAction.commonOk(::dismissDialog))
            override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_warning)
            override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_tooOldMessage)
            override val customContent:
                @Composable
                (() -> Unit)? = null
        }
    }

    private fun displayErrorDialog(error: Throwable?) {
        _dialogState.value = ErrorDialogState(
            error = error,
            actions = listOf(
                DialogAction.commonOk(::dismissDialog),
            ),
            dismiss = ::dismissDialog,
        )
    }

    fun dismissDialog() {
        _dialogState.value = null
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getResetMessage(): String? {
        return contactId.value?.let {
            getResetMessageUseCase(contactId = it).data?.let(Base64::encode)
        }
    }

    /**
     * Create a zip archive from the message to send
     * @param messageToSend [String] the message to send (encoded to base64)
     * @return [File] the file with the compressed message (use the decoded data inside the file)
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun createAndShareArchive(messageToSend: String, share: (File) -> Unit) {
        viewModelScope.launch {
            val file = loadingManager.withLoading {
                val archiveResult = createBubblesMessageArchiveUseCase(
                    messageData = Base64.decode(messageToSend),
                    attachmentFile = null,
                ).first { it !is LBFlowResult.Loading }
                when (archiveResult) {
                    is LBFlowResult.Loading -> null
                    is LBFlowResult.Failure -> {
                        displayErrorDialog(archiveResult.throwable)
                        null
                    }
                    is LBFlowResult.Success -> archiveResult.successData
                }
            }
            file?.let { share(it) }
        }
    }

    fun consumeArchive() {
        deleteBubblesArchiveUseCase()
    }

    fun askForSafeConfirmation(sentMessageDataUnderSharing: SentMessageData) {
        viewModelScope.launch {
            if (getAppVisitUseCase.hasSeenDialogMessageSaveConfirmation()) {
                _snackbarState.value = SaveMessageConfirmationSnackbarState(
                    onClick = {
                        saveEncryptedMessage(sentMessageDataUnderSharing)
                    },
                )
            } else {
                setAppVisitUseCase.setHasSeenDialogMessageSaveConfirmation()
                _dialogState.value = SaveMessageConfirmationDialogState(
                    onConfirm = { saveEncryptedMessage(sentMessageDataUnderSharing) },
                    dismiss = { _dialogState.value = null },
                )
            }
        }
    }
}
