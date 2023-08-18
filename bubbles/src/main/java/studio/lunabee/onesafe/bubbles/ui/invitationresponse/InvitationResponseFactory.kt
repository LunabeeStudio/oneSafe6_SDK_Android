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
 * Last modified 20/07/2023 11:57
 */

package studio.lunabee.onesafe.bubbles.ui.invitationresponse

import androidx.compose.foundation.lazy.LazyListScope
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

object InvitationResponseFactory {
    fun explanationCard(
        contactName: String,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSMessageCard(
                description = LbcTextSpec.StringResource(
                    R.string.bubbles_invitationResponseScreen_explanation,
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
                description = LbcTextSpec.StringResource(R.string.bubbles_invitationResponseScreen_shareDescription),
                action = {
                    OSClickableRow(
                        text = LbcTextSpec.StringResource(R.string.bubbles_invitationResponseScreen_share),
                        onClick = onShareClick,
                        buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                        leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = R.drawable.ic_share)) },
                        contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                            index = 1,
                            elementsCount = 2,
                        ),
                    )
                },
            )
        }
    }
}
