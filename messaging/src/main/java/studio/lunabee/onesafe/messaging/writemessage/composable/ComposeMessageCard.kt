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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 2:56 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallDivider
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.messaging.R
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSColor
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import java.util.UUID

@Composable
fun ComposeMessageCard(
    plainMessage: String,
    encryptedMessage: String,
    onPlainMessageChange: (String) -> Unit,
    onClickOnSend: () -> Unit,
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
            OSTextField(
                value = plainMessage,
                onValueChange = onPlainMessageChange,
                modifier = Modifier
                    .fillMaxWidth(),
                label = LbcTextSpec.StringResource(R.string.oneSafeK_composeMessageCard_label),
                placeholder = LbcTextSpec.StringResource(R.string.oneSafeK_composeMessageCard_label),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = OSColor.Neutral80,
                    focusedContainerColor = OSColor.Neutral80,
                    focusedIndicatorColor = OSColor.Neutral80,
                    unfocusedIndicatorColor = OSColor.Neutral80,
                    focusedTextColor = OSColor.Neutral10,
                    unfocusedTextColor = OSColor.Neutral10,
                    focusedLabelColor = OSColor.Neutral80,
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                modifier = Modifier.padding(start = OSDimens.SystemSpacing.Regular),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    tint = OSColor.Neutral60,
                )
                OSSmallDivider(
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(bottom = OSDimens.SystemSpacing.Regular),
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val context = LocalContext.current
                OSText(
                    text = LbcTextSpec.Raw(encryptedMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = OSColor.Neutral60,
                    maxLines = EncryptedMessageMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // TODO Temporary action used to test oneSafeK feature.
                            val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val myClip: ClipData = ClipData.newPlainText("copy", encryptedMessage)
                            myClipboard.setPrimaryClip(myClip)
                        },
                )

                OSIconButton(
                    image = OSImageSpec.Drawable(R.drawable.ic_send),
                    onClick = onClickOnSend,
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    contentDescription = LbcTextSpec.StringResource(R.string.accessibility_oneSafeK_sendAction),
                    colors = OSIconButtonDefaults.primaryIconButtonColors(),
                )
            }
        }
    }
}

private const val EncryptedMessageMaxLines: Int = 2

@Preview
@Composable
fun ComposeMessageCardPreview() {
    OSPreviewBackgroundTheme {
        ComposeMessageCard(
            plainMessage = "This is a plain message",
            encryptedMessage = UUID.randomUUID().toString(),
            onPlainMessageChange = {},
            onClickOnSend = {},
        )
    }
}
