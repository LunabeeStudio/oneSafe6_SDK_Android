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
 * Created by Lunabee Studio / Date - 9/1/2023 - for the oneSafe6 SDK.
 * Last modified 01/09/2023 17:35
 */

package studio.lunabee.onesafe.bubbles.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.bubbles.ui.BubblesUiConstants
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun NotificationIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(BubblesUiConstants.NotificationIndicatorSize)
            .clip(CircleShape)
            .testTag(UiConstants.TestTag.Item.NotificationIndicator)
            .background(BubblesUiConstants.NotificationColor),
    )
}
