/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/22/2024 - for the oneSafe6 SDK.
 * Last modified 4/22/24, 11:07 AM
 */

package studio.lunabee.onesafe.importexport.settings.backupnumber

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoBackupMaxNumberBottomSheetContent(
    paddingValues: PaddingValues,
    onSelect: (entry: AutoBackupMaxNumber) -> Unit,
    selectedAutoBackupMaxNumber: AutoBackupMaxNumber,
) {
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .selectableGroup()
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(OSString.settings_autoBackupMaxNumberScreen_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        OSText(
            text = LbcTextSpec.StringResource(OSString.settings_autoBackupMaxNumberScreen_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
        )
        AutoBackupMaxNumber.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = { onSelect(entry) },
                isSelected = entry == selectedAutoBackupMaxNumber,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun AutoBackupMaxNumberBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupMaxNumberBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            onSelect = {},
            selectedAutoBackupMaxNumber = AutoBackupMaxNumber.FIVE,
        )
    }
}
