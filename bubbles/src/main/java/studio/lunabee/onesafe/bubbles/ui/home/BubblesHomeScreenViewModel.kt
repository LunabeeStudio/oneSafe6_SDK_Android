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
 * Created by Lunabee Studio / Date - 7/10/2023 - for the oneSafe6 SDK.
 * Last modified 10/07/2023 15:30
 */

package studio.lunabee.onesafe.bubbles.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbextensions.enumValueOfOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.bubbles.domain.BubblesConstant
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.bubbles.ui.model.BubbleContactInfo
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.messaging.domain.usecase.GetConversationStateUseCase
import javax.inject.Inject

@HiltViewModel
class BubblesHomeScreenViewModel @Inject constructor(
    getEncryptedBubblesContactList: GetAllContactsUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val messageRepository: MessageRepository,
    val osFeatureFlags: FeatureFlags,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _conversation = MutableStateFlow<List<BubblesConversationInfo>?>(null)
    val conversation: StateFlow<List<BubblesConversationInfo>?> = _conversation.asStateFlow()

    private val _contacts: MutableStateFlow<List<UIBubblesContactInfo>?> = MutableStateFlow(null)
    val contacts: StateFlow<List<UIBubblesContactInfo>?> = _contacts.asStateFlow()

    val initialTab: BubblesHomeTab? = enumValueOfOrNull<BubblesHomeTab>(savedStateHandle[BubblesHomeDestination.BubblesHomeTabArg])

    init {
        viewModelScope.launch {
            getEncryptedBubblesContactList().collect { encryptedContacts ->
                val contactLists = encryptedContacts.map { contact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(contact.encName, contact.id, String::class)
                    BubbleContactInfo(
                        id = contact.id,
                        conversationState = getConversationStateUseCase.invoke(contact.id),
                        isConvReady = contact.encSharedKey != null,
                        rawName = decryptedNameResult,
                        updatedAt = contact.updatedAt,
                    )
                }
                _contacts.value = contactLists.sortedBy { it.rawName.data }.map {
                    UIBubblesContactInfo(
                        id = it.id,
                        conversationState = it.conversationState,
                        nameProvider = it.rawName.getNameProvider(),
                    )
                }
                _conversation.value = contactLists.sortedByDescending { it.updatedAt }.map { info ->
                    val lastMessage = messageRepository.getLastMessage(info.id).firstOrNull()
                    BubblesConversationInfo(
                        nameProvider = info.rawName.getNameProvider(),
                        subtitle = if (info.isConvReady) {
                            lastMessage?.let { message ->
                                val content = contactLocalDecryptUseCase(message.encContent, info.id, String::class).data.orEmpty()
                                val realContent = if (content == BubblesConstant.FirstMessageData) {
                                    LbcTextSpec.StringResource(R.string.bubbles_acceptedInvitation)
                                } else {
                                    LbcTextSpec.Raw(content)
                                }
                                ConversationSubtitle.Message(content = realContent)
                            }
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
}
