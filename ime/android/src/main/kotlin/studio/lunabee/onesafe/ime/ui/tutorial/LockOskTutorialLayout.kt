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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 15/11/2023 10:30
 */

package studio.lunabee.onesafe.ime.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ime.ImeOSTheme
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ime.R as ImeR

@Composable
fun LockOskTutorialLayout(
    onCloseClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .padding(top = OSDimens.SystemSpacing.Regular)
            .padding(horizontal = OSDimens.SystemSpacing.Small),
    ) {
        ImeTutorialCard(
            title = LbcTextSpec.StringResource(OSString.oneSafeK_tutorial_lock_title),
            description = LbcTextSpec.StringResource(OSString.oneSafeK_tutorial_lock_description),
            modifier = Modifier.weight(1f),
            onClose = onCloseClick,
        )
        OSImage(
            image = OSImageSpec.Drawable(ImeR.drawable.right_handdraw_arrow),
        )
    }
}

@Preview
@Composable
private fun LockOskTutorialLayoutPreview() {
    ImeOSTheme {
        LockOskTutorialLayout {}
    }
}
