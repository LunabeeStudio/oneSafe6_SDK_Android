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
 * Created by Lunabee Studio / Date - 8/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/08/2024 16:18
 */

package studio.lunabee.onesafe.feature.importbackup.bubbleswarning

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.feature.importbackup.savedelegate.ImportSaveDataDelegate
import studio.lunabee.onesafe.feature.importbackup.savedelegate.ImportSaveDataDelegateImpl
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import javax.inject.Inject

@HiltViewModel
class ImportBubblesWarningViewModel @Inject constructor(
    importEngine: ImportEngine,
    private val importSaveDataDelegate: ImportSaveDataDelegateImpl,
) : ViewModel(), ImportSaveDataDelegate by importSaveDataDelegate {

    val hasItemsToImports: Boolean = importEngine.hasItemsToImport

    fun saveBubblesData() {
        importSaveDataDelegate.launchImport(mode = ImportMode.Append)
    }

    override fun onCleared() {
        super.onCleared()
        importSaveDataDelegate.close()
    }
}
