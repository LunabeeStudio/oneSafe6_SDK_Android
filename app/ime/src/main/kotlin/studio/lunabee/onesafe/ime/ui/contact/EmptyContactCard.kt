/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/29/23, 6:19 PM
 */

package studio.lunabee.onesafe.ime.ui.contact

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.LocalCardContentExtraSpace
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun EmptyContactCard() {
    OSTopImageBox(
        imageRes = R.drawable.character_jamy_cool,
    ) {
        CompositionLocalProvider(LocalCardContentExtraSpace.provides(null)) {
            OSMessageCard(
                modifier = Modifier.testTag(UiConstants.TestTag.Item.BubblesNoContactCard),
                description = LbcTextSpec.StringResource(id = R.string.bubbles_emptyContactCard_description),
                title = LbcTextSpec.StringResource(id = R.string.bubbles_emptyContactCard_title),
                contentAlignment = Alignment.BottomCenter,
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
