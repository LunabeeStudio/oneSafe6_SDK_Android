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
 * Created by Lunabee Studio / Date - 6/15/2023 - for the oneSafe6 SDK.
 * Last modified 6/15/23, 9:24 AM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall

@Composable
fun ConversationDayHeader(
    text: LbcTextSpec,
) {
    OSText(
        text = text,
        style = MaterialTheme.typography.labelXSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(vertical = OSDimens.SystemSpacing.ExtraSmall)
            .clip(RoundedCornerShape(OSDimens.SystemCornerRadius.Regular))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = OSDimens.SystemSpacing.ExtraSmall, horizontal = OSDimens.SystemSpacing.Small),
    )
}

@Preview
@Composable
fun ConversationDayHeaderPreview() {
    OSPreviewBackgroundTheme {
        Box(
            modifier = Modifier.padding(OSDimens.SystemSpacing.Medium),
        ) {
            ConversationDayHeader(text = LbcTextSpec.Raw("Today"))
        }
    }
}
