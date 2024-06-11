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
 * Created by Lunabee Studio / Date - 5/29/2023 - for the oneSafe6 SDK.
 * Last modified 5/29/23, 4:51 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.screen

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.messaging.writemessage.model.BubblesWritingMessage
import java.util.UUID

// TODO <bubbles> error state (no contact found on deeplink for example)

@Stable
sealed interface WriteMessageUiState {
    data object Initializing : WriteMessageUiState
    data class Data(
        val contactId: UUID,
        val nameProvider: OSNameProvider,
        val message: BubblesWritingMessage,
        val isUsingDeepLink: Boolean,
        val isConversationReady: Boolean,
        val isCorrupted: Boolean,
    ) : WriteMessageUiState
}
