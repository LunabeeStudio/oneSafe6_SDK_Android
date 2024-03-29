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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.bubbles.domain.BubblesConstant
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.bubbles.ui.invitation.InvitationUiState
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage
import studio.lunabee.onesafe.messaging.domain.usecase.EncryptMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetSendMessageDataUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.SaveSentMessageUseCase
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvitationResponseViewModel @Inject constructor(
    private val getSendMessageDataUseCase: GetSendMessageDataUseCase,
    private val getContactUseCase: GetContactUseCase,
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val encryptMessageUseCase: EncryptMessageUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val saveSentMessageUseCase: SaveSentMessageUseCase,
    private val messageIdProvider: MessageIdProvider,
    settings: OSAppSettings,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val contactId: UUID = savedStateHandle.get<String>(InvitationResponseDestination.ContactIdArgs)?.let {
        UUID.fromString(it)
    }
        ?: error("Missing contact id in args")

    val isMaterialYouEnabled: Flow<Boolean> = settings.materialYouSetting

    private val _uiState: MutableStateFlow<InvitationUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<InvitationUiState?> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                getContactUseCase(contactId).first()?.encName?.let { name ->
                    val decryptedNameResult = contactLocalDecryptUseCase(
                        name,
                        contactId,
                        String::class,
                    )
                    val (message, isFirstMessage) = generateMessage()
                    _uiState.value = InvitationUiState.Data(
                        invitationString = message,
                        contactName = decryptedNameResult.data.orEmpty(),
                    )
                    if (isFirstMessage) {
                        saveMessageInDatabase(message)
                    }
                }
            } catch (error: OSError) {
                _dialogState.value = ErrorDialogState(
                    error = error,
                    actions = listOf(DialogAction.commonOk(::exitScreen)),
                )
            }
        }
    }

    // Return the string to send and if we need to save the message if its the first message we send
    private suspend fun generateMessage(): Pair<String, Boolean> {
        val messageData = getSendMessageDataUseCase(contactId)
        return when (messageData) {
            is LBResult.Failure -> throw messageData.throwable ?: error("an error append")
            is LBResult.Success -> {
                val plainMessage = BubblesConstant.FirstMessageData
                val encryptResult = encryptMessageUseCase(
                    plainMessage,
                    contactId,
                    Instant.now(),
                    messageData.successData,
                )
                when (encryptResult) {
                    is LBResult.Failure -> throw encryptResult.throwable ?: error("an error append")
                    is LBResult.Success -> encryptResult.successData to (messageData.successData.messageHeader.messageNumber == 0)
                }
            }
        }
    }

    private fun saveMessageInDatabase(messageData: String) {
        viewModelScope.launch {
            val messageId = messageIdProvider()
            val messageOrder = saveMessageUseCase(
                plainMessage = OSPlainMessage(
                    content = BubblesConstant.FirstMessageData,
                    recipientId = contactId,
                    sentAt = Instant.now(),
                ),
                contactId = contactId,
                channel = null,
                id = messageId,
            ).data
            messageOrder?.let {
                saveSentMessageUseCase.invoke(
                    contactId = contactId,
                    id = messageId,
                    messageString = messageData,
                    createdAt = Instant.now(),
                    order = messageOrder,
                )
            }
        }
    }

    private fun exitScreen() {
        _dialogState.value = null
        _uiState.value = InvitationUiState.Exit
    }
}
