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
 * Created by Lunabee Studio / Date - 5/23/2023 - for the oneSafe6 SDK.
 * Last modified 5/23/23, 5:11 PM
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.LocalCardContentExtraSpace
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSSmallDivider
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.bubbles.R
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.commonui.ResourcesLibrary
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun EmptyContactCard() {
    OSTopImageBox(
        imageRes = ResourcesLibrary.characterJamyCool,
    ) {
        CompositionLocalProvider(LocalCardContentExtraSpace.provides(null)) {
            OSMessageCard(
                modifier = Modifier.testTag(UiConstants.TestTag.Item.BubblesNoContactCard),
                description = LbcTextSpec.StringResource(id = R.string.bubbles_emptyContactCard_description),
                title = LbcTextSpec.StringResource(id = R.string.bubbles_emptyContactCard_title),
                contentAlignment = Alignment.BottomCenter,
                action = {
                    Column(
                        modifier = Modifier.padding(top = OSDimens.SystemSpacing.Regular),
                    ) {
                        OSSmallDivider(
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                        )
                        OSClickableRow(
                            text = LbcTextSpec.StringResource(id = R.string.bubbles_contact_manageButton),
                            onClick = {},
                            buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                            leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = ResourcesLibrary.icPeople)) },
                        )
                    }
                },
            )
        }
    }
}

@Composable
@Preview
fun EmptyContactCardPreview() {
    OSPreviewBackgroundTheme {
        EmptyContactCard()
    }
}
