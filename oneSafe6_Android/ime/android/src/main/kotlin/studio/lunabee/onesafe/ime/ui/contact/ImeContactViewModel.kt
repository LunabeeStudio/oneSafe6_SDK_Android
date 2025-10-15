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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 2:19 PM
 */

package studio.lunabee.onesafe.ime.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.messaging.domain.model.ConversationState
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.CommonUiConstants
import javax.inject.Inject

@HiltViewModel
class ImeContactViewModel @Inject constructor(
    getEncryptedBubblesContactList: GetAllContactsUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
) : ViewModel() {

    val uiState: StateFlow<ImeContactUiState> = getEncryptedBubblesContactList()
        .map { encryptedContacts ->
            if (encryptedContacts.isEmpty()) {
                ImeContactUiState.Empty
            } else {
                val plainContacts = encryptedContacts.map { contact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(contact.encName, contact.id, String::class)
                    val conversationState = getConversationStateUseCase(contact.id)

                    when (conversationState) {
                        is LBResult.Failure -> UIBubblesContactInfo(
                            id = contact.id,
                            nameProvider = decryptedNameResult.getNameProvider(),
                            isConversationReady = true, // default to true to not display specific info
                        )
                        is LBResult.Success -> {
                            UIBubblesContactInfo(
                                id = contact.id,
                                nameProvider = decryptedNameResult.getNameProvider(),
                                isConversationReady = conversationState.successData != ConversationState.WaitingForReply,
                            )
                        }
                    }
                }
                ImeContactUiState.Data(plainContacts)
            }
        }.stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            ImeContactUiState.Initializing,
        )
}
