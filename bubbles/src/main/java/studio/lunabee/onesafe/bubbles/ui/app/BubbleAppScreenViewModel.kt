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

package studio.lunabee.onesafe.bubbles.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.domain.common.FeatureFlags
import javax.inject.Inject

@HiltViewModel
class BubbleAppScreenViewModel @Inject constructor(
    getEncryptedBubblesContactList: GetAllContactsUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    val osFeatureFlags: FeatureFlags,
) : ViewModel() {

    private val _conversation = MutableStateFlow<List<BubblesContactInfo>?>(null)
    val conversation: StateFlow<List<BubblesContactInfo>?> = _conversation.asStateFlow()

    private val _contacts: MutableStateFlow<List<BubblesContactInfo>?> = MutableStateFlow(null)
    val contacts: StateFlow<List<BubblesContactInfo>?> = _contacts.asStateFlow()

    init {
        viewModelScope.launch {
            getEncryptedBubblesContactList().collect { encryptedContacts ->
                val contactLists = encryptedContacts.map { contact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(contact.encName, contact.id, String::class)
                    Pair(
                        BubblesContactInfo(id = contact.id, nameProvider = decryptedNameResult.getNameProvider()),
                        // Use to know if we display the contact in the conversation tab
                        contact.encSharedKey != null,
                    )
                }
                _contacts.value = contactLists.map { it.first }
                _conversation.value = contactLists.filter { it.second }.map { it.first }
            }
        }
    }
}
