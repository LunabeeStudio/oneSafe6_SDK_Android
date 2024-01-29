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
 * Last modified 20/07/2023 11:33
 */

package studio.lunabee.onesafe.bubbles.ui.invitation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

object InvitationScreenFactory {
    fun explanationCard(
        contactName: String,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSMessageCard(
                description = LbcTextSpec.StringResource(
                    OSString.bubbles_invitationScreen_explanation,
                    contactName,
                ),
            )
        }
    }

    fun sharedCard(
        lazyListScope: LazyListScope,
        onShareClick: () -> Unit,
    ) {
        lazyListScope.item {
            OSMessageCard(
                description = LbcTextSpec.StringResource(OSString.bubbles_invitationScreen_description),
                action = {
                    OSClickableRow(
                        text = LbcTextSpec.StringResource(OSString.bubbles_invitationScreen_share),
                        onClick = onShareClick,
                        buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                        leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = OSDrawable.ic_share)) },
                        contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                            index = 1,
                            elementsCount = 2,
                        ),
                    )
                },
            )
        }
    }

    fun finishButtonScreen(
        onClick: () -> Unit,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.bubbles_invitationScreen_continueButton),
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}
