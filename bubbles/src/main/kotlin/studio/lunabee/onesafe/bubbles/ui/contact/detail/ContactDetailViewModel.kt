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
 * Created by Lunabee Studio / Date - 7/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/07/2023 11:23
 */

package studio.lunabee.onesafe.bubbles.ui.contact.detail

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.onesafe.bubbles.domain.usecase.UpdateIsUsingDeeplinkContactUseCase
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.GetConversationStateUseCase
import studio.lunabee.onesafe.ui.extensions.getFirstColorGenerated
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getContactUseCase: GetContactUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val contactRepository: ContactRepository,
    private val updateIsUsingDeeplinkContactUseCase: UpdateIsUsingDeeplinkContactUseCase,
    private val getConversationStateUseCase: GetConversationStateUseCase,
    private val imageHelper: ImageHelper,
    isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) : ViewModel() {
    val contactId: UUID = savedStateHandle.get<String>(ContactDetailDestination.ContactIdArg)?.let { UUID.fromString(it) }
        ?: error("Missing contact id in args")

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    private val _uiState: MutableStateFlow<ContactDetailUiState> = MutableStateFlow(ContactDetailUiState.Idle)
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        combine(
            getContactUseCase.flow(contactId),
            isCryptoDataReadyInMemoryUseCase.flow(),
        ) { encryptedContacts, isCryptoDataReadyInMemory ->
            encryptedContacts?.let {
                val decryptedNameResult = contactLocalDecryptUseCase(
                    encryptedContacts.encName,
                    encryptedContacts.id,
                    String::class,
                )
                val decryptedIsUsingDeeplink = contactLocalDecryptUseCase(
                    encryptedContacts.encIsUsingDeeplink,
                    encryptedContacts.id,
                    Boolean::class,
                )
                val conversationState = getConversationStateUseCase(contactId)

                when (conversationState) {
                    is LBResult.Failure -> {
                        if (isCryptoDataReadyInMemory) {
                            showError(conversationState.throwable)
                        }

                        _uiState.value = ContactDetailUiState.Data(
                            id = encryptedContacts.id,
                            nameProvider = decryptedNameResult.getNameProvider(),
                            isDeeplinkActivated = decryptedIsUsingDeeplink.data ?: false,
                            conversationState = ContactDetailUiState.UIConversationState.Indecipherable,
                            color = decryptedNameResult.data?.let { plainColor ->
                                getColorFromName(plainColor)
                            },
                        )
                    }
                    is LBResult.Success -> {
                        _uiState.value = ContactDetailUiState.Data(
                            id = encryptedContacts.id,
                            nameProvider = decryptedNameResult.getNameProvider(),
                            isDeeplinkActivated = decryptedIsUsingDeeplink.data ?: false,
                            conversationState = ContactDetailUiState.UIConversationState.fromConversationState(
                                conversationState.successData,
                            ),
                            color = decryptedNameResult.data?.let { plainColor ->
                                getColorFromName(plainColor)
                            },
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun getColorFromName(name: String): Color? {
        val emoji = name.startEmojiOrNull()
        return emoji?.let {
            imageHelper.createBitmapWithText(emoji)?.let { bitmapWithText ->
                imageHelper.extractColorPaletteFromBitmap(bitmapWithText).getFirstColorGenerated()
            }
        }
    }

    fun updateIsUsingDeeplink(value: Boolean) {
        viewModelScope.launch {
            val result = updateIsUsingDeeplinkContactUseCase(contactId, value)
            if (result is LBResult.Failure) showError(result.throwable)
        }
    }

    fun deleteContact() {
        _dialogState.value = ConfirmDeleteContactDialogState(
            dismiss = { _dialogState.value = null },
            deleteAction = {
                viewModelScope.launch {
                    contactRepository.deleteContact(contactId)
                    _uiState.value = ContactDetailUiState.Exit
                }
            },
        )
    }

    private fun showError(error: Throwable?) {
        _dialogState.value = ErrorDialogState(
            error = error,
            actions = listOf(
                DialogAction.commonOk { _dialogState.value = null },
            ),
            dismiss = { _dialogState.value = null },
        )
    }
}
