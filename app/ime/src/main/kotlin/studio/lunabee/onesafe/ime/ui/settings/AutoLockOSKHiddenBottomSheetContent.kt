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
 * Created by Lunabee Studio / Date - 9/26/2023 - for the oneSafe6 SDK.
 * Last modified 9/26/23, 5:34 PM
 */

package studio.lunabee.onesafe.ime.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoLockOSKHiddenBottomSheetContent(
    onSelect: (entry: AutoLockBackgroundDelay) -> Unit,
    selectedAutoLockAppChangeDelay: AutoLockBackgroundDelay,
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.oneSafeK_byChangingAppAutolockScreen_header_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(id = OSString.oneSafeK_byChangingAppAutolockScreen_footer_title),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        AutoLockBackgroundDelay.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = {
                    onSelect(entry)
                },
                isSelected = entry == selectedAutoLockAppChangeDelay,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun AutoLockOSKHiddenBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        AutoLockOSKHiddenBottomSheetContent(
            onSelect = {},
            selectedAutoLockAppChangeDelay = AutoLockBackgroundDelay.IMMEDIATELY,
        )
    }
}
