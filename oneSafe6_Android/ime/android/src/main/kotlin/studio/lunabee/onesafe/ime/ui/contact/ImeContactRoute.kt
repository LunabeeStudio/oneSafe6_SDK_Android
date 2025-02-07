/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 2:17 PM
 */

package studio.lunabee.onesafe.ime.ui.contact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.ime.ui.OSImeScreen
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

@Composable
fun ImeContactRoute(
    navigateBack: () -> Unit,
    navigateToWriteMessage: (UUID) -> Unit,
    viewModel: ImeContactViewModel = hiltViewModel(),
    exitIcon: Int,
    deeplinkBubblesHomeContact: () -> Unit,
    deeplinkBubblesWriteMessage: (UUID) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ImeContactUiState.Data -> {
            ImeContactFilledScreen(
                uiState = state,
                navigateBack = navigateBack,
                onClickOnContact = { contactId, isConversationReady ->
                    if (isConversationReady) {
                        navigateToWriteMessage(contactId)
                    } else {
                        deeplinkBubblesWriteMessage(contactId)
                    }
                },
                exitIcon = exitIcon,
                navigateToBubblesHomeContact = deeplinkBubblesHomeContact,
            )
        }
        ImeContactUiState.Empty -> {
            ImeContactEmptyScreen(
                navigateBack = navigateBack,
                navigateToBubblesHomeContact = deeplinkBubblesHomeContact,
            )
        }
        ImeContactUiState.Initializing -> {
            OSImeScreen(
                testTag = UiConstants.TestTag.Screen.FilledContactScreen,
                background = LocalDesignSystem.current.bubblesBackGround(),
            ) {
                Box(Modifier.fillMaxSize())
            }
        }
    }
}
