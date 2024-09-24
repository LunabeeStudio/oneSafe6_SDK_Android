/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/3/2024 - for the oneSafe6 SDK.
 * Last modified 03/09/2024 11:27
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall

@Composable
fun ResetConversationRow(
    modifier: Modifier = Modifier,
) {
    OSTheme(
        isSystemInDarkTheme = true,
        isMaterialYouSettingsEnabled = LocalDesignSystem.current.isMaterialYouEnabled,
    ) {
        OSText(
            text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_conversationReset),
            style = MaterialTheme.typography.labelXSmall,
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(LocalColorPalette.current.Neutral80)
                .padding(
                    horizontal = OSDimens.SystemSpacing.Small,
                    vertical = OSDimens.SystemSpacing.ExtraSmall,
                ),
            color = LocalColorPalette.current.Neutral10,
        )
    }
}
