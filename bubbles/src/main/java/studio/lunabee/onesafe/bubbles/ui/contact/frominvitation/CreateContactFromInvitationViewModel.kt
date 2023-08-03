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
 * Created by Lunabee Studio / Date - 7/17/2023 - for the oneSafe6 SDK.
 * Last modified 17/07/2023 10:59
 */

package studio.lunabee.onesafe.bubbles.ui.contact.frominvitation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.ui.contact.creation.CreateContactViewModel
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.usecase.AcceptInvitationUseCase
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class CreateContactFromInvitationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val acceptInvitationUseCase: AcceptInvitationUseCase,
) : ViewModel(), CreateContactViewModel {

    private val messageString: String = savedStateHandle.get<String>(CreateContactFromInvitationDestination.MessageString)
        ?: error("Missing message string in args")

    private val _createInvitationResult: MutableStateFlow<LBResult<UUID>?> = MutableStateFlow(null)
    override val createInvitationResult: StateFlow<LBResult<UUID>?> = _createInvitationResult.asStateFlow()

    @OptIn(ExperimentalEncodingApi::class)
    override fun createContact(
        contactName: String,
        isUsingDeeplink: Boolean,
    ) {
        viewModelScope.launch {
            _createInvitationResult.value = OSError.runCatching {
                try {
                    acceptInvitationUseCase(contactName, isUsingDeeplink, Base64.decode(messageString))
                } catch (e: IllegalArgumentException) {
                    throw OSDomainError(OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64)
                }
            }
        }
    }
}
