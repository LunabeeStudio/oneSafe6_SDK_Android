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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.ContactScreenFactory
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
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
            onClick = onConversationClick,
        )
        lazyVerticalOSRegularSpacer()
        item {
            OSCard(modifier = Modifier.fillMaxWidth()) {
                OSRow(
                    text = LbcTextSpec.StringResource(R.string.bubbles_decryptMessage),
                    startContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_message),
                            contentDescription = null,
                        )
                    },
                    endContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_navigate_next),
                            tint = LocalColorPalette.current.Neutral30,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier
                        .clickable(onClick = onDecryptClick)
                        .padding(
                            start = OSDimens.SystemSpacing.Small,
                            top = OSDimens.SystemSpacing.Small,
                            bottom = OSDimens.SystemSpacing.Small,
                        )
                        .padding(
                            LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                index = 0,
                                elementsCount = if (isOSKShown) 2 else 1,
                            ),
                        ),
                )
                if (isOSKShown) {
                    OSRow(
                        text = LbcTextSpec.StringResource(R.string.bubbles_configureOneSafeK),
                        secondaryText = LbcTextSpec.StringResource(R.string.bubbles_configureOneSafeK_subtitle),
                        textMaxLines = Int.MAX_VALUE,
                        startContent = {
                            Icon(painter = painterResource(id = R.drawable.ic_settings), contentDescription = null)
                        },
                        endContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_navigate_next),
                                tint = LocalColorPalette.current.Neutral30,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier
                            .clickable(onClick = onSettingClick)
                            .padding(
                                start = OSDimens.SystemSpacing.Small,
                                top = OSDimens.SystemSpacing.Small,
                                bottom = OSDimens.SystemSpacing.Small,
                            )
                            .padding(LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(index = 1, elementsCount = 2)),
                    )
                }
            }
        }
    }
}
