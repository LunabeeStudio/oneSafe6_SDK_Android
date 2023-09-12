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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 3:38 PM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun OSImeScreen(
    testTag: String,
    modifier: Modifier = Modifier,
    background: Brush = LocalDesignSystem.current.backgroundGradient(),
    applySystemBarPadding: Boolean = false,
    content: @Composable (BoxScope.() -> Unit),
) {
    val embeddedKeyboardHeight: Dp = LocalKeyboardUiHeight.current

    OSScreen(
        testTag = testTag,
        modifier = if (embeddedKeyboardHeight != 0.dp) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        }
            .landscapeSystemBarsPadding()
            .then(modifier),
        background = background,
        applySystemBarPadding = applySystemBarPadding,
        content = content,
    )
}
