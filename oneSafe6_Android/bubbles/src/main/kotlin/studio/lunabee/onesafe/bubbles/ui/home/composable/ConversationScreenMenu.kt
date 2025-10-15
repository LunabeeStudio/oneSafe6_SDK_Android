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
 * Created by Lunabee Studio / Date - 10/3/2024 - for the oneSafe6 SDK.
 * Last modified 03/10/2024 09:53
 */

package studio.lunabee.onesafe.bubbles.ui.home.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun ConversationScreenMenu(
    onDecryptClick: () -> Unit,
    onSettingClick: () -> Unit,
    isOSKShown: Boolean,
    modifier: Modifier = Modifier,
) {
    OSCard(modifier = modifier.fillMaxWidth()) {
        OSRow(
            text = LbcTextSpec.StringResource(OSString.bubbles_decryptMessage),
            startContent = {
                Icon(
                    painter = painterResource(id = OSDrawable.ic_message),
                    contentDescription = null,
                )
            },
            endContent = {
                Icon(
                    painter = painterResource(id = OSDrawable.ic_navigate_next),
                    tint = LocalColorPalette.current.neutral30,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .clickable(onClick = onDecryptClick)
                .padding(
                    start = OSDimens.SystemSpacing.Small,
                    top = OSDimens.SystemSpacing.Small,
                    bottom = OSDimens.SystemSpacing.Small,
                ).padding(
                    LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 0,
                        elementsCount = if (isOSKShown) 2 else 1,
                    ),
                ),
        )
        if (isOSKShown) {
            OSRow(
                text = LbcTextSpec.StringResource(OSString.bubbles_configureOneSafeK),
                secondaryText = LbcTextSpec.StringResource(OSString.bubbles_configureOneSafeK_subtitle),
                textMaxLines = Int.MAX_VALUE,
                startContent = {
                    Icon(painter = painterResource(id = OSDrawable.ic_settings), contentDescription = null)
                },
                endContent = {
                    Icon(
                        painter = painterResource(id = OSDrawable.ic_navigate_next),
                        tint = LocalColorPalette.current.neutral30,
                        contentDescription = null,
                    )
                },
                modifier = Modifier
                    .clickable(onClick = onSettingClick)
                    .padding(
                        start = OSDimens.SystemSpacing.Small,
                        top = OSDimens.SystemSpacing.Small,
                        bottom = OSDimens.SystemSpacing.Small,
                    ).padding(
                        LocalDesignSystem.current
                            .getRowClickablePaddingValuesDependingOnIndex(index = 1, elementsCount = 2),
                    ),
            )
        }
    }
}
