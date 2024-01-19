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
 * Created by Lunabee Studio / Date - 8/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/08/2023 10:14
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSChipStyle
import studio.lunabee.onesafe.atom.OSChipType
import studio.lunabee.onesafe.atom.OSInputChip
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingInputChip(
    modifier: Modifier = Modifier,
) {
    OSInputChip(
        modifier = modifier,
        selected = true,
        onClick = null,
        type = OSChipType.Progress,
        style = OSChipStyle.Small,
        label = {
            OSText(
                text = LbcTextSpec.StringResource(R.string.common_pending),
            )
        },
    )
}
