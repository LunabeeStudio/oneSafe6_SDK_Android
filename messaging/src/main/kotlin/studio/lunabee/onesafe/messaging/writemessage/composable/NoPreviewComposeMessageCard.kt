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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 31/08/2023 14:57
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.localprovider.LocalOneSafeKImeController
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.messaging.MessagingConstants
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette

@Composable
fun NoPreviewComposeMessageCard(
    plainMessage: TextFieldValue,
    onPlainMessageChange: (TextFieldValue) -> Unit,
    onClickOnSend: () -> Unit,
    sendIcon: OSImageSpec,
    focusRequester: FocusRequester,
    canSend: Boolean,
) {
    val isKeyboardVisible: Boolean = LocalOneSafeKImeController.current.isVisible
    Box(
        modifier = Modifier
            .background(LocalColorPalette.current.Neutral70)
            .landscapeSystemBarsPadding(),
    ) {
        OSCard(
            colors = CardDefaults.cardColors(
                containerColor = LocalColorPalette.current.Neutral80,
            ),
            modifier = Modifier
                .padding(
                    horizontal = OSDimens.SystemSpacing.Small,
                    vertical = OSDimens.SystemSpacing.Regular,
                )
                .then(
                    if (isKeyboardVisible) {
                        Modifier
                    } else {
                        Modifier.navigationBarsPadding()
                    },
                ),
        ) {
            Row(
                modifier = Modifier
                    .padding(end = OSDimens.SystemSpacing.Regular),
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OSTextField(
                    textFieldValue = plainMessage,
                    onValueChange = onPlainMessageChange,
                    modifier = Modifier
                        .weight(1.0f)
                        .focusRequester(focusRequester),
                    label = null,
                    placeholder = LbcTextSpec.StringResource(OSString.oneSafeK_composeMessageCard_label),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = LocalColorPalette.current.Neutral80,
                        focusedContainerColor = LocalColorPalette.current.Neutral80,
                        focusedIndicatorColor = LocalColorPalette.current.Neutral80,
                        unfocusedIndicatorColor = LocalColorPalette.current.Neutral80,
                        focusedTextColor = LocalColorPalette.current.Neutral10,
                        unfocusedTextColor = LocalColorPalette.current.Neutral10,
                        unfocusedPlaceholderColor = LocalColorPalette.current.Neutral60,
                        focusedLabelColor = LocalColorPalette.current.Neutral60,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions {
                        if (plainMessage.text.isNotEmpty()) onClickOnSend()
                    },
                    maxLines = MessagingConstants.MessageTextFieldMaxLines,
                )
                OSIconButton(
                    image = sendIcon,
                    onClick = onClickOnSend,
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    contentDescription = LbcTextSpec.StringResource(OSString.accessibility_oneSafeK_sendAction),
                    colors = OSIconButtonDefaults.primaryIconButtonColors(),
                    state = when {
                        plainMessage.text.isEmpty() -> OSActionState.Disabled
                        canSend -> OSActionState.Enabled
                        else -> OSActionState.DisabledWithAction
                    },
                )
            }
        }
    }
}
