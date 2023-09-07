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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSColor

@Composable
fun NoPreviewComposeMessageCard(
    plainMessage: String,
    onPlainMessageChange: (String) -> Unit,
    onClickOnSend: () -> Unit,
    sendIcon: OSImageSpec,
) {
    val embeddedKeyboardHeight: Dp = LocalKeyboardUiHeight.current
    Box(
        modifier = Modifier
            .background(OSColor.Neutral70)
            .landscapeSystemBarsPadding(),
    ) {
        OSCard(
            colors = CardDefaults.cardColors(
                containerColor = OSColor.Neutral80,
            ),
            modifier = Modifier
                .padding(
                    horizontal = OSDimens.SystemSpacing.Small,
                    vertical = OSDimens.SystemSpacing.Regular,
                )
                .then(
                    if (embeddedKeyboardHeight == 0.dp) {
                        Modifier.navigationBarsPadding()
                    } else {
                        Modifier
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
                    value = plainMessage,
                    onValueChange = onPlainMessageChange,
                    modifier = Modifier
                        .weight(1.0f),
                    label = null,
                    placeholder = LbcTextSpec.StringResource(R.string.oneSafeK_composeMessageCard_label),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = OSColor.Neutral80,
                        focusedContainerColor = OSColor.Neutral80,
                        focusedIndicatorColor = OSColor.Neutral80,
                        unfocusedIndicatorColor = OSColor.Neutral80,
                        focusedTextColor = OSColor.Neutral10,
                        unfocusedTextColor = OSColor.Neutral10,
                        unfocusedPlaceholderColor = OSColor.Neutral60,
                        focusedLabelColor = OSColor.Neutral60,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions {
                        if (plainMessage.isNotEmpty()) onClickOnSend()
                    },
                )
                OSIconButton(
                    image = sendIcon,
                    onClick = onClickOnSend,
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    contentDescription = LbcTextSpec.StringResource(R.string.accessibility_oneSafeK_sendAction),
                    colors = OSIconButtonDefaults.primaryIconButtonColors(),
                    state = if (plainMessage.isEmpty()) OSActionState.Disabled else OSActionState.Enabled,
                )
            }
        }
    }
}
