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
 * Created by Lunabee Studio / Date - 7/20/2023 - for the oneSafe6 SDK.
 * Last modified 20/07/2023 10:34
 */

package studio.lunabee.onesafe.bubbles.ui.contact.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.molecule.OSLargeItemTitle
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

object ContactDetailScreenFactory {

    fun conversationStateCard(
        conversationState: ContactDetailUiState.UIConversationState,
        contactName: OSNameProvider,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            when (conversationState) {
                ContactDetailUiState.UIConversationState.FullySetup,
                ContactDetailUiState.UIConversationState.WaitingForFirstMessage,
                -> {
                    OSMessageCard(
                        title = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_congratulation_title),
                        description = LbcTextSpec.StringResource(
                            OSString.bubbles_contactDetail_congratulation_description,
                            contactName.name,
                        ),
                    )
                }
                ContactDetailUiState.UIConversationState.WaitingForReply -> {
                    OSMessageCard(
                        title = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_waitingForReply_title),
                        description = LbcTextSpec.StringResource(
                            OSString.bubbles_contactDetail_waitingForReply_description,
                            contactName.name,
                        ).markdown(),
                    )
                }
                ContactDetailUiState.UIConversationState.Running -> {}
                ContactDetailUiState.UIConversationState.Indecipherable -> OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_corruptedCard_title),
                    description = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_corruptedCard_description),
                )
            }
        }
    }

    fun removeContactCard(
        onClick: () -> Unit,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OSClickableRow(
                    text = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_deleteContact),
                    onClick = onClick,
                    buttonColors = OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled),
                    leadingIcon = { OSIconAlertDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_delete)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 0,
                        elementsCount = 1,
                    ),
                )
            }
        }
    }

    fun title(
        nameProvider: OSNameProvider,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSLargeItemTitle(
                title = nameProvider.name,
                icon = if (nameProvider is EmojiNameProvider) {
                    OSItemIllustration.Emoji(nameProvider.placeholderName, null)
                } else {
                    OSItemIllustration.Text(nameProvider.placeholderName, null)
                },
                modifier = Modifier.padding(vertical = OSDimens.SystemSpacing.Regular),
            )
        }
    }

    @Suppress("LongParameterList")
    fun actionCard(
        conversationState: ContactDetailUiState.UIConversationState,
        onConversationClick: () -> Unit,
        onResendInvitationClick: () -> Unit,
        onResendResponseClick: () -> Unit,
        onScanResponseClick: () -> Unit,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OSClickableRow(
                    text = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_sendMessage),
                    onClick = onConversationClick,
                    buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(
                        state = if (conversationState != ContactDetailUiState.UIConversationState.WaitingForReply) {
                            OSActionState.Enabled
                        } else {
                            OSActionState.Disabled
                        },
                    ),
                    state = if (conversationState != ContactDetailUiState.UIConversationState.WaitingForReply) {
                        OSActionState.Enabled
                    } else {
                        OSActionState.Disabled
                    },
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_message)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 0,
                        elementsCount = if (
                            (conversationState == ContactDetailUiState.UIConversationState.FullySetup) ||
                            (conversationState == ContactDetailUiState.UIConversationState.Running)
                        ) {
                            1
                        } else {
                            2
                        },
                    ),
                )
                when (conversationState) {
                    ContactDetailUiState.UIConversationState.Running,
                    ContactDetailUiState.UIConversationState.FullySetup,
                    ContactDetailUiState.UIConversationState.Indecipherable,
                    -> {
                        /* no-op */
                    }
                    ContactDetailUiState.UIConversationState.WaitingForReply -> {
                        OSClickableRow(
                            text = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_resendInvitation),
                            onClick = onResendInvitationClick,
                            buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                            leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_people)) },
                            contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                index = 1,
                                elementsCount = 3,
                            ),
                        )
                        OSClickableRow(
                            text = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_scanAnswer),
                            onClick = onScanResponseClick,
                            buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                            leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_qr_scanner)) },
                            contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                index = 2,
                                elementsCount = 3,
                            ),
                        )
                    }
                    ContactDetailUiState.UIConversationState.WaitingForFirstMessage -> {
                        OSClickableRow(
                            text = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_resendResponse),
                            onClick = onResendResponseClick,
                            buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                            leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_people)) },
                            contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                index = 1,
                                elementsCount = 2,
                            ),
                        )
                    }
                }
            }
        }
    }
}
