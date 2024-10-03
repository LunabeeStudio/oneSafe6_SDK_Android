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
 * Created by Lunabee Studio / Date - 8/22/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 10:22
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.UpdateContactUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.bubbles.ui.contact.form.common.ContactFormState
import studio.lunabee.onesafe.bubbles.ui.contact.form.common.ContactFormViewModel
import studio.lunabee.onesafe.bubbles.ui.contact.form.common.DefaultContactFormDelegate
import studio.lunabee.onesafe.bubbles.ui.contact.model.MessageSharingModeUi
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    editContactDelegate: EditContactDelegate,
    contactRepository: ContactRepository,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    savedStateHandle: SavedStateHandle,
) : ContactFormViewModel(editContactDelegate) {
    private val contactId: DoubleRatchetUUID = savedStateHandle.get<String>(EditContactDestination.ContactIdArgs)
        ?.let { DoubleRatchetUUID.fromString(it) }
        ?: error("Missing contact id in args")

    init {
        viewModelScope.launch {
            val contact = contactRepository.getContactFlow(contactId).firstOrNull()
            contact?.let {
                val decryptedNameResult = contactLocalDecryptUseCase(
                    contact.encName,
                    contact.id,
                    String::class,
                ).data
                val decryptedSharingMode = contactLocalDecryptUseCase(
                    contact.encSharingMode,
                    contact.id,
                    MessageSharingMode::class,
                ).data?.let { MessageSharingModeUi.fromMode(it) }
                mFormState.value = ContactFormState(
                    name = decryptedNameResult.orEmpty(),
                    sharingMessageMode = decryptedSharingMode ?: MessageSharingModeUi.Deeplinks,
                )
            }
        }
    }
}

class EditContactDelegate @Inject constructor(
    private val updateContactUseCase: UpdateContactUseCase,
    savedStateHandle: SavedStateHandle,
    loadingManager: LoadingManager,
) : DefaultContactFormDelegate(loadingManager) {
    private val contactId: DoubleRatchetUUID = savedStateHandle.get<String>(EditContactDestination.ContactIdArgs)
        ?.let { DoubleRatchetUUID.fromString(it) }
        ?: error("Missing contact id in args")

    private val _createInvitationResult: MutableStateFlow<LBResult<DoubleRatchetUUID>?> = MutableStateFlow(null)
    override val createInvitationResult: StateFlow<LBResult<DoubleRatchetUUID>?> = _createInvitationResult.asStateFlow()
    override suspend fun doSaveContact(contactName: String, sharingMode: MessageSharingMode) {
        val updateRes: LBResult<Unit> = updateContactUseCase(contactId, sharingMode, contactName)
        when (updateRes) {
            is LBResult.Failure -> _createInvitationResult.value = LBResult.Failure(updateRes.throwable, contactId)
            is LBResult.Success -> _createInvitationResult.value = LBResult.Success(contactId)
        }
    }
}
