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
 * Last modified 10/3/23, 2:09 PM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder

@Composable
fun AutoBackupFrequencyBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (entry: AutoBackupFrequency) -> Unit,
    selectedAutoBackupFrequency: AutoBackupFrequency,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, _ ->
        AutoBackupFrequencyBottomSheetContent(
            onSelect = { frequency ->
                onSelect(frequency)
                closeBottomSheet()
            },
            selectedAutoBackupFrequency = selectedAutoBackupFrequency,
        )
    }
}
