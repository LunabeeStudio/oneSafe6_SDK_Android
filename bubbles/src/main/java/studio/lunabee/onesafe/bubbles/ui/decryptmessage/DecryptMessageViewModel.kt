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
 * Created by Lunabee Studio / Date - 7/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/07/2023 10:01
 */

package studio.lunabee.onesafe.bubbles.ui.decryptmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.ui.extension.getBase64FromMessage
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.usecase.ManageIncomingMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.ManagingIncomingMessageResultData
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class DecryptMessageViewModel @Inject constructor(
    private val manageIncomingMessageUseCase: ManageIncomingMessageUseCase,
) : ViewModel() {

    private val _uiResultState: MutableStateFlow<DecryptMessageUiState> = MutableStateFlow(DecryptMessageUiState.Idle)
    val uiResultState: StateFlow<DecryptMessageUiState> = _uiResultState.asStateFlow()

    @OptIn(ExperimentalEncodingApi::class)
    fun handleMessage(message: String) {
        viewModelScope.launch {
            val messageString = message.getBase64FromMessage()
            try {
                val data = Base64.decode(messageString)
                val result = manageIncomingMessageUseCase(data, null)
                when (result) {
                    is LBResult.Success -> {
                        _uiResultState.value = when (val resultData = result.successData) {
                            is ManagingIncomingMessageResultData.Invitation -> DecryptMessageUiState.NavigateToCreateContact(messageString)
                            is ManagingIncomingMessageResultData.Message -> {
                                DecryptMessageUiState.NavigateToConversation(resultData.data.first.id)
                            }
                        }
                    }
                    is LBResult.Failure -> _uiResultState.value = DecryptMessageUiState.Error(result.throwable as? OSError)
                }
            } catch (e: IllegalArgumentException) {
                _uiResultState.value = DecryptMessageUiState.Error(OSDomainError(OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64))
            }
        }
    }
}
