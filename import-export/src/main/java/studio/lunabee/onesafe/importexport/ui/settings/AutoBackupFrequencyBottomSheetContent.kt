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
 * Created by Lunabee Studio / Date - 10/4/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 2:10 PM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.molecule.OSOptionRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
internal fun AutoBackupFrequencyBottomSheetContent(
    onSelect: (entry: AutoBackupFrequency) -> Unit,
    selectedAutoBackupFrequency: AutoBackupFrequency,
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            text = LbcTextSpec.StringResource(id = R.string.settings_autoBackupFrequencyScreen_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
        )
        AutoBackupFrequency.entries.forEach { entry ->
            OSOptionRow(
                text = entry.text,
                onSelect = { onSelect(entry) },
                isSelected = entry == selectedAutoBackupFrequency,
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun AutoBackupFrequencyBottomSheetContentPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupFrequencyBottomSheetContent(
            onSelect = {},
            selectedAutoBackupFrequency = AutoBackupFrequency.WEEKLY,
        )
    }
}
