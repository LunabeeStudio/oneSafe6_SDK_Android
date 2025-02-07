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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBackupMaxNumberBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (entry: AutoBackupMaxNumber) -> Unit,
    selectedAutoBackupMaxNumber: AutoBackupMaxNumber,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        AutoBackupMaxNumberBottomSheetContent(
            paddingValues = paddingValues,
            onSelect = { maxNumber ->
                onSelect(maxNumber)
                closeBottomSheet()
            },
            selectedAutoBackupMaxNumber = selectedAutoBackupMaxNumber,
        )
    }
}
