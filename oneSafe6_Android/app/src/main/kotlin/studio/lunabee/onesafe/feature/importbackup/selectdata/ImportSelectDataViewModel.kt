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
 * Last modified 27/08/2024 11:08
 */

package studio.lunabee.onesafe.feature.importbackup.selectdata

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegate
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportGetMetaDataDelegateImpl
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import javax.inject.Inject

@HiltViewModel
class ImportSelectDataViewModel @Inject constructor(
    private val importEngine: ImportEngine,
    importSaveDataMetaDataDelegateImpl: ImportGetMetaDataDelegateImpl,
) : ViewModel(), ImportGetMetaDataDelegate by importSaveDataMetaDataDelegateImpl {
    private val _isBubblesImported: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isBubblesImported: StateFlow<Boolean> = _isBubblesImported.asStateFlow()

    private val _isItemsImported: MutableStateFlow<Boolean> = MutableStateFlow(metadataResult.data?.itemCount != 0)
    val isItemsImported: StateFlow<Boolean> = _isItemsImported.asStateFlow()

    val numberOfItemsToImport: Int = metadataResult.data?.itemCount ?: 0

    fun setImportBubbles(value: Boolean) {
        _isBubblesImported.value = value
    }

    fun setImportItems(value: Boolean) {
        _isItemsImported.value = value
    }

    fun setImportData() {
        importEngine.setDataToImport(isBubblesImported.value, isItemsImported.value)
    }
}
