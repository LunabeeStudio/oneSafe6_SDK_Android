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
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.ime.ui.contact.ImeContactViewModel
import javax.inject.Inject

class SelectContactViewModelFactory @Inject constructor(
    private val getEncryptedBubblesContactList: GetAllContactsUseCase,
    private val decryptForContactUseCase: ContactLocalDecryptUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
) : AbstractSavedStateViewModelFactory() {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        @Suppress("UNCHECKED_CAST")
        return ImeContactViewModel(getEncryptedBubblesContactList, decryptForContactUseCase, getConversationStateUseCase) as T
    }
}
