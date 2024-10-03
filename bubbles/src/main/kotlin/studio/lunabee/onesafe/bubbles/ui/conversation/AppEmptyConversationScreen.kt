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
 * Created by Lunabee Studio / Date - 7/11/2023 - for the oneSafe6 SDK.
 * Last modified 11/07/2023 10:51
 */

package studio.lunabee.onesafe.bubbles.ui.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.home.composable.ConversationScreenMenu
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun AppEmptyConversationScreen(
    onDecryptClick: () -> Unit,
    onSettingClick: () -> Unit,
    isOSKShown: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        OSTopImageBox(
            imageRes = OSDrawable.character_sabine_oups_center,
            modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
            offset = null,
        ) {
            OSMessageCard(
                title = LbcTextSpec.StringResource(OSString.bubbles_noContact_title),
                description = LbcTextSpec.StringResource(OSString.bubbles_noContact_subtitle),
            )
        }
        ConversationScreenMenu(
            onDecryptClick = onDecryptClick,
            onSettingClick = onSettingClick,
            isOSKShown = isOSKShown,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
        )
        OSRegularSpacer()
    }
}
