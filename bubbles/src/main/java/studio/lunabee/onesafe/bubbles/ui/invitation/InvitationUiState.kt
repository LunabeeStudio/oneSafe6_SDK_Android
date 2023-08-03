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
 * Last modified 13/07/2023 13:48
 */

package studio.lunabee.onesafe.bubbles.ui.invitation

import studio.lunabee.onesafe.commonui.OSNameProvider

sealed interface InvitationUiState {
    data class Data(
        val contactName: OSNameProvider,
        val invitationString: String,
    ) : InvitationUiState

    object Exit : InvitationUiState
}
