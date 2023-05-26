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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.manager

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.usecase.search.DecryptIndexWordUseCase
import javax.inject.Inject

class SearchIndexManager @Inject constructor(
    private val indexWordEntryRepository: IndexWordEntryRepository,
    private val decryptIndexWordUseCase: DecryptIndexWordUseCase,
) {
    private val _decryptedIndex: MutableStateFlow<LBFlowResult<List<PlainIndexWordEntry>>> = MutableStateFlow(LBFlowResult.Loading())
    val decryptedIndex: StateFlow<LBFlowResult<List<PlainIndexWordEntry>>> = _decryptedIndex.asStateFlow()

    private var storeIndexJob: Job? = null
    private var cleanStoreIndexJob: Job? = null

    fun initStoreIndex(scope: CoroutineScope) {
        if (storeIndexJob?.isActive != true) {
            storeIndexJob = readAndStoreIndex(scope)
        }
        clearIndexAfterDelay(scope)
    }

    private fun readAndStoreIndex(scope: CoroutineScope): Job = scope.launch {
        indexWordEntryRepository.getAll().collect {
            _decryptedIndex.value = LBFlowResult.Loading()
            _decryptedIndex.value = decryptIndexWordUseCase(it).asFlowResult()
        }
    }

    private fun clearIndexAfterDelay(execScope: CoroutineScope) {
        cleanStoreIndexJob?.cancel(RESET_TIMER_CLEAR_INDEX_REASON)
        cleanStoreIndexJob = execScope.launch {
            delay(DELAY_CLEAR_DECRYPTED_INDEX)
            storeIndexJob?.cancel(CANCEL_SEARCH_CAUSE_NOT_USED)
            // TODO : Clean correctly the decrypted index
            _decryptedIndex.value = LBFlowResult.Loading()
        }
    }

    companion object {
        const val DELAY_CLEAR_DECRYPTED_INDEX: Long = 20_000
        private const val RESET_TIMER_CLEAR_INDEX_REASON: String = "Job cancelled  for resetting the clear index countdown"
        private const val CANCEL_SEARCH_CAUSE_NOT_USED: String = "Collect index Job cancelled  for resetting the clear index countdown"
    }
}
