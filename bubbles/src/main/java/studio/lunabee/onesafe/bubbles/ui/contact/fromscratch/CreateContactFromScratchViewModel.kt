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
 * Last modified 17/07/2023 10:45
 */

package studio.lunabee.onesafe.bubbles.ui.contact.fromscratch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.bubbles.ui.contact.creation.CreateContactViewModel
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.messaging.domain.usecase.CreateInvitationUseCase
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateContactFromScratchViewModel @Inject constructor(
    private val createInvitationUseCase: CreateInvitationUseCase,
) : ViewModel(), CreateContactViewModel {

    private val _createInvitationResult: MutableStateFlow<LBResult<UUID>?> = MutableStateFlow(null)
    override val createInvitationResult: StateFlow<LBResult<UUID>?> = _createInvitationResult.asStateFlow()

    override fun createContact(
        contactName: String,
        isUsingDeeplink: Boolean,
    ) {
        viewModelScope.launch {
            _createInvitationResult.value = OSError.runCatching {
                createInvitationUseCase(contactName, isUsingDeeplink)
            }
        }
    }
}
