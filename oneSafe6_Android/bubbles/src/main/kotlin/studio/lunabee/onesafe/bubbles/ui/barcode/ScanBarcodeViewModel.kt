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
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 13/07/2023 16:44
 */

package studio.lunabee.onesafe.bubbles.ui.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import studio.lunabee.messaging.domain.usecase.ManageIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.ManagingIncomingMessageResultData
import studio.lunabee.onesafe.bubbles.ui.extension.getBase64FromMessage
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@HiltViewModel
class ScanBarcodeViewModel @Inject constructor(
    private val manageIncomingMessageUseCase: ManageIncomingMessageUseCase,
) : ViewModel() {

    private val _uiResultState: MutableStateFlow<ScanBarcodeUiState> = MutableStateFlow(ScanBarcodeUiState.Idle)
    val uiResultState: StateFlow<ScanBarcodeUiState> = _uiResultState.asStateFlow()

    private val _dialogState = MutableStateFlow<DialogState?>(null)
    val dialogState: StateFlow<DialogState?> get() = _dialogState.asStateFlow()

    @OptIn(ExperimentalEncodingApi::class)
    fun handleQrCode(text: String) {
        if (uiResultState.value == ScanBarcodeUiState.Idle) {
            viewModelScope.launch {
                mutex.withLock {
                    val messageString = text.getBase64FromMessage()
                    try {
                        val data = Base64.decode(messageString)
                        val result = manageIncomingMessageUseCase(data, null)
                        when (result) {
                            is LBResult.Success -> {
                                _uiResultState.value = when (val resultData = result.successData) {
                                    is ManagingIncomingMessageResultData.Invitation ->
                                        ScanBarcodeUiState
                                            .NavigateToCreateContact(
                                                messageString,
                                            )
                                    is ManagingIncomingMessageResultData.Message -> {
                                        ScanBarcodeUiState.NavigateToConversation(resultData.decryptResult)
                                    }
                                    is ManagingIncomingMessageResultData.SafeItem -> error("should not append")
                                }
                            }
                            is LBResult.Failure -> {
                                _dialogState.value = ErrorDialogState(
                                    error = result.throwable,
                                    actions = listOf(DialogAction.commonOk(::dismissDialog)),
                                )
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        // ignore base64 errors
                    }
                }
            }
        }
    }

    private fun dismissDialog() {
        _dialogState.value = null
    }

    companion object {
        private val mutex = Mutex()
    }
}
