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
 * Created by Lunabee Studio / Date - 7/10/2023 - for the oneSafe6 SDK.
 * Last modified 10/07/2023 17:28
 */

package studio.lunabee.onesafe.bubbles.ui.conversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.ContactScreenFactory
import studio.lunabee.onesafe.bubbles.ui.home.composable.ConversationScreenMenu
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

@Composable
fun AppFilledConversationScreen(
    contacts: List<BubblesConversationInfo>,
    onConversationClick: (UUID) -> Unit,
    onDecryptClick: () -> Unit,
    onSettingClick: () -> Unit,
    isOSKShown: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
    ) {
        ContactScreenFactory.addConversations(
            conversations = contacts,
            onClick = { onConversationClick(it.uuid) },
        )
        lazyVerticalOSRegularSpacer()
        item {
            ConversationScreenMenu(
                onDecryptClick = onDecryptClick,
                onSettingClick = onSettingClick,
                isOSKShown = isOSKShown,
            )
        }
    }
}
