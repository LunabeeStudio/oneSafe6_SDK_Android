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
 * Created by Lunabee Studio / Date - 7/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/07/2023 12:21
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ConversationNotReadyCard(
    contactName: String,
    onResendInvitationClick: () -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.weight(1.0f))
        Image(
            painter = painterResource(id = R.drawable.character_jamy_phone),
            contentDescription = null,
            modifier = Modifier.weight(1.0f),
            contentScale = ContentScale.FillHeight,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding(),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(
                    R.string.bubbles_conversationScreen_notReadyMessage,
                    contactName,
                ),
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.ExtraLarge)
                    .padding(top = OSDimens.SystemSpacing.ExtraLarge, bottom = OSDimens.SystemSpacing.Regular),
            )
            OSTextButton(
                text = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_resendInvitation),
                onClick = onResendInvitationClick,
                buttonColors = OSTextButtonDefaults.primaryTextButtonColors(state = OSActionState.Enabled),
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Large)
                    .padding(bottom = OSDimens.SystemSpacing.ExtraLarge),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun ConversationNotReadyCardPreview() {
    OSPreviewBackgroundTheme {
        ConversationNotReadyCard(loremIpsum(2), {})
    }
}
