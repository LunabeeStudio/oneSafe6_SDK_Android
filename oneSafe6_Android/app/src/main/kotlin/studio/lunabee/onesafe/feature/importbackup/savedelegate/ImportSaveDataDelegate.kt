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
 * Last modified 27/08/2024 16:35
 */

package studio.lunabee.onesafe.feature.importbackup.savedelegate

import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.importexport.usecase.ImportSaveDataUseCase
import java.io.File
import javax.inject.Inject

interface ImportSaveDataDelegate {
    val importSaveDataState: StateFlow<ImportSaveDataUiState>
    fun launchImport(mode: ImportMode)
    fun setState(state: ImportSaveDataUiState)
    fun resetState()
}

class ImportSaveDataDelegateImpl @Inject constructor(
    private val importSaveDataUseCase: ImportSaveDataUseCase,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import) private val archiveDir: File,
) : ImportSaveDataDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {
    private val loadingDelegate: LBLoadingVisibilityDelayDelegate = LBLoadingVisibilityDelayDelegate()
    private val _importSaveDataState: MutableStateFlow<ImportSaveDataUiState> =
        MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
    override val importSaveDataState: StateFlow<ImportSaveDataUiState> = _importSaveDataState.asStateFlow()

    override fun launchImport(mode: ImportMode) {
        coroutineScope.launch {
            importSaveDataUseCase(
                archiveExtractedDirectory = archiveDir,
                mode = mode,
            ).collect { importResult ->
                when (importResult) {
                    is LBFlowResult.Failure -> _importSaveDataState.value = ImportSaveDataUiState.Dialog(
                        dialogState = ErrorDialogState(
                            error = importResult.throwable,
                            actions = listOf(
                                DialogAction(
                                    text = LbcTextSpec.StringResource(id = OSString.common_retry),
                                    type = DialogAction.Type.Normal,
                                    onClick = {
                                        _importSaveDataState.value = ImportSaveDataUiState.WaitingForUserChoice
                                    },
                                ),
                            ),
                            dismiss = {
                                _importSaveDataState.value = ImportSaveDataUiState.WaitingForUserChoice
                            },
                        ),
                    )
                    is LBFlowResult.Loading -> loadingDelegate.delayShowLoading {
                        _importSaveDataState.value = ImportSaveDataUiState.ImportInProgress(importResult.progress ?: .0f)
                    }
                    is LBFlowResult.Success -> loadingDelegate.delayHideLoading {
                        _importSaveDataState.value = ImportSaveDataUiState.Success
                    }
                }
            }
        }
    }

    override fun setState(state: ImportSaveDataUiState) {
        _importSaveDataState.value = state
    }

    override fun resetState() {
        _importSaveDataState.value = ImportSaveDataUiState.WaitingForUserChoice
    }
}
