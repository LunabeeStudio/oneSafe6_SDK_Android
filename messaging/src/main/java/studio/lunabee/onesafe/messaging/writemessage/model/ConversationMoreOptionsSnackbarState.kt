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
 * Created by Lunabee Studio / Date - 9/6/2023 - for the oneSafe6 SDK.
 * Last modified 9/6/23, 8:42 AM
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.snackbar.SnackbarAction
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState

class ConversationMoreOptionsSnackbarState(
    private val deeplinkBubblesWriteMessage: () -> Unit,
) : SnackbarState(
    SnackbarAction.Default(
        onClick = deeplinkBubblesWriteMessage,
        actionLabel = LbcTextSpec.StringResource(R.string.common_open),
        onDismiss = {},
    ),
) {
    override val message: LbcTextSpec = LbcTextSpec.StringResource(R.string.oneSafeK_conversationScreen_snackbar_moreOptions)

    @Composable
    fun SnackBar(snackBarHostState: SnackbarHostState) {
        val visuals = snackbarVisuals
        LaunchedEffect(visuals) {
            when (snackBarHostState.showSnackbar(visuals)) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> deeplinkBubblesWriteMessage()
            }
        }
    }
}
