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
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo

// TODO bubbles real UiState
//  • currentContact must not be null
//  • error state (no contact found on deeplink for example)
//  • initializing state ?
//  • remove default values

@Stable
data class WriteMessageUiState(
    val currentContact: UIBubblesContactInfo? = null,
    val plainMessage: String = "",
    val encryptedPreview: String = "",
    val isUsingDeepLink: Boolean = false,
    val isConversationReady: Boolean = true,
)
