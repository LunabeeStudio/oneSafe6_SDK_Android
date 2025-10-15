/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/7/2024 - for the oneSafe6 SDK.
 * Last modified 07/08/2024 09:21
 */

package studio.lunabee.onesafe.messaging.senditem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.bubbles.ui.conversation.ConversationSubtitleFromMessageDelegate
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.bubbles.ui.model.BubbleContactInfo
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetItemUseCase
import studio.lunabee.onesafe.messaging.senditem.model.FileShareData
import studio.lunabee.onesafe.messaging.senditem.model.SharedItemInfo
import studio.lunabee.onesafe.messaging.usecase.CreateMessageWithItemSharedUseCase
import studio.lunabee.onesafe.messaging.usecase.DeleteBubblesArchiveUseCase
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@HiltViewModel
class SendItemBubblesSelectContactViewModel @Inject constructor(
    getEncryptedBubblesContactList: GetAllContactsUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val messageRepository: MessageRepository,
    private val conversationSubtitleFromMessageDelegate: ConversationSubtitleFromMessageDelegate,
    private val createMessageWithItemSharedUseCase: CreateMessageWithItemSharedUseCase,
    private val loadingManager: LoadingManager,
    private val countSafeItemUseCase: CountSafeItemUseCase,
    private val secureGetItemUseCase: SecureGetItemUseCase,
    private val itemDecryptUseCase: ItemDecryptUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val clock: Clock,
    private val deleteBubblesArchiveUseCase: DeleteBubblesArchiveUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val itemId: UUID = savedStateHandle
        .get<String>(SendItemBubblesSelectContactDestination.ItemToShareIdArgument)
        ?.let(UUID::fromString) ?: error("missing argument")
    private val includeChildren: Boolean = savedStateHandle.get<Boolean>(SendItemBubblesSelectContactDestination.IncludeChildrenArgument)
        ?: error("missing argument")

    private val _conversation = MutableStateFlow<List<BubblesConversationInfo>?>(null)
    val conversation: StateFlow<List<BubblesConversationInfo>?> = _conversation.asStateFlow()

    private val _fileToShare: MutableStateFlow<FileShareData?> = MutableStateFlow(null)
    val fileToShare: StateFlow<FileShareData?> = _fileToShare.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    val sharedItemInfo: StateFlow<SharedItemInfo> = flow {
        val safeItem: SafeItem? = secureGetItemUseCase.invoke(itemId).firstOrNull()
        val safeItemName = safeItem?.encName?.let { itemDecryptUseCase.invoke(it, itemId, String::class) }
        emit(
            if (includeChildren) {
                SharedItemInfo.WithChildren(
                    numberOfChild = countSafeItemUseCase.notDeleted(itemId).getOrThrow(),
                    name = LbcTextSpec.Raw(safeItemName?.data.orEmpty()),
                )
            } else {
                SharedItemInfo.NoChildren(
                    name = LbcTextSpec.Raw(safeItemName?.data.orEmpty()),
                )
            },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SharedItemInfo
            .NoChildren(LbcTextSpec.StringResource(OSString.common_loading)),
    )

    init {
        viewModelScope.launch {
            getEncryptedBubblesContactList().collect { encryptedContacts ->
                val contactLists = encryptedContacts.map { contact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(contact.encName, contact.id, String::class)
                    val conversationState = getConversationStateUseCase.invoke(contact.id)
                    BubbleContactInfo(
                        id = contact.id,
                        conversationState = conversationState,
                        isConversationReady = contact.encSharedKey != null,
                        plainName = decryptedNameResult,
                        updatedAt = contact.updatedAt.toJavaInstant(),
                    )
                }
                _conversation.value = contactLists.sortedByDescending { it.updatedAt }.map { info ->
                    val lastMessage = messageRepository.getLastMessage(info.id).firstOrNull()
                    BubblesConversationInfo(
                        nameProvider = info.plainName.getNameProvider(),
                        subtitle = if (info.isConversationReady) {
                            lastMessage?.let { conversationSubtitleFromMessageDelegate(lastMessage) }
                        } else {
                            ConversationSubtitle.NotReady
                        },
                        id = info.id,
                        hasUnreadMessage = lastMessage?.isRead == false,
                    )
                }
            }
        }
    }

    private fun displayErrorDialog(error: Throwable?) {
        _dialogState.value = ErrorDialogState(
            error = error,
            dismiss = { _dialogState.value = null },
            actions = listOf(DialogAction.commonOk { _dialogState.value = null }),
        )
    }

    fun getMessageToSend(contactId: DoubleRatchetUUID) {
        viewModelScope.launch {
            loadingManager.withLoading {
                val result = createMessageWithItemSharedUseCase
                    .invoke(
                        contactId = contactId,
                        itemId = itemId,
                        includeChildren = includeChildren,
                    ).first { it !is LBFlowResult.Loading }
                    .asResult()
                when (result) {
                    is LBResult.Failure -> displayErrorDialog(result.throwable)
                    is LBResult.Success -> _fileToShare.value = FileShareData(
                        file = result.successData,
                        contactId = contactId,
                        safeItemId = DoubleRatchetUUID(itemId),
                    )
                }
            }
        }
    }

    fun saveMessage(fileShareData: FileShareData) {
        viewModelScope.launch {
            saveMessageUseCase(
                plainMessage = SharedMessage(
                    content = MessagingConstant.SafeItemMessageData,
                    recipientId = fileShareData.contactId,
                    date = Instant.now(clock).toKotlinInstant(),
                ),
                contactId = fileShareData.contactId,
                id = createRandomUUID(),
                channel = null,
                safeItemId = fileShareData.safeItemId,
            )
        }
    }

    fun consumeArchive() {
        _fileToShare.value = null
        deleteBubblesArchiveUseCase()
    }
}
