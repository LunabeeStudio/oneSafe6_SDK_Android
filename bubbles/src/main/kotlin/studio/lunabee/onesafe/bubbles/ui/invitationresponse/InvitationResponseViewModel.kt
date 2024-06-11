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
 * Last modified 17/07/2023 11:17
 */

package studio.lunabee.onesafe.bubbles.ui.invitationresponse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.bubbles.ui.invitation.InvitationUiState
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.OSMessagingError
import studio.lunabee.onesafe.messaging.domain.usecase.GetInvitationResponseMessageUseCase
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvitationResponseViewModel @Inject constructor(
    private val getContactUseCase: GetContactUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    settings: OSAppSettings,
    savedStateHandle: SavedStateHandle,
    private val getInvitationResponseMessageUseCase: GetInvitationResponseMessageUseCase,
) : ViewModel() {
    val contactId: UUID = savedStateHandle.get<String>(InvitationResponseDestination.ContactIdArgs)?.let {
        UUID.fromString(it)
    } ?: error("Missing contact id in args")

    val isMaterialYouEnabled: Flow<Boolean> = settings.materialYouSetting

    private val _uiState: MutableStateFlow<InvitationUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<InvitationUiState?> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    init {
        initializeWithMessage()
    }

    private fun initializeWithMessage() {
        viewModelScope.launch {
            val messageResult = getInvitationResponseMessageUseCase(contactId)
            when (messageResult) {
                is LBResult.Failure -> exitWithError(messageResult.throwable)
                is LBResult.Success -> {
                    val contact = getContactUseCase(contactId)
                    if (contact == null) {
                        exitWithError(OSMessagingError.Code.CONTACT_NOT_FOUND.get())
                    } else {
                        val nameResult = contactLocalDecryptUseCase(
                            contact.encName,
                            contactId,
                            String::class,
                        )
                        when (nameResult) {
                            is LBResult.Failure -> exitWithError(nameResult.throwable)
                            is LBResult.Success -> {
                                _uiState.value = InvitationUiState.Data(
                                    invitationString = messageResult.successData,
                                    contactName = nameResult.successData,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun exitWithError(error: Throwable?) {
        _dialogState.value = ErrorDialogState(
            error = error,
            actions = listOf(
                DialogAction.commonOk {
                    _dialogState.value = null
                    _uiState.value = InvitationUiState.Exit
                },
            ),
        )
    }
}
