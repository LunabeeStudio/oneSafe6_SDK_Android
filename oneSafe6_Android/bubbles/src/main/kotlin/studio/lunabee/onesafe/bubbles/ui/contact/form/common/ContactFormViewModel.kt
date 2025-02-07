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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 23/08/2023 09:38
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.onesafe.bubbles.ui.contact.model.MessageSharingModeUi

open class ContactFormViewModel(
    private val contactFormDelegate: ContactFormDelegate,
) : ViewModel(),
    ContactFormDelegate by contactFormDelegate {

    protected val mFormState: MutableStateFlow<ContactFormState> = MutableStateFlow(ContactFormState())
    val formState: StateFlow<ContactFormState> = mFormState.asStateFlow()

    fun setName(value: String) {
        mFormState.value = mFormState.value.copy(name = value)
    }

    fun setModeSharingMessage(value: MessageSharingModeUi) {
        mFormState.value = mFormState.value.copy(sharingMessageMode = value)
    }

    override fun onCleared() {
        super.onCleared()
        contactFormDelegate.close()
    }
}
