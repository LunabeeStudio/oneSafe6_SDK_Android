/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/5/2023 - for the oneSafe6 SDK.
 * Last modified 4/12/23, 4:02 PM
 */

package studio.lunabee.onesafe.domain.usecase.search

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.manager.SearchIndexManager
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.model.search.SearchQuery
import javax.inject.Inject

/**
 * Run the search and provide results in [searchResultFlow]. This usecase is responsible of:
 *   - The index initialization
 *   - The delay before the search if the query is not final (see finalQuery param).
 *   - Search query filtering based on [Constant.MinimumCharForSearch]
 * The actual matching is done by the [getMatchFromSearchUseCase]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchItemUseCase @Inject constructor(
    private val getMatchFromSearchUseCase: GetMatchFromSearchUseCase,
    private val searchIndexManager: SearchIndexManager,
) {
    var result: LinkedHashSet<SafeItemWithIdentifier>? = null
    private val searchQueryFlow: MutableStateFlow<SearchQuery> = MutableStateFlow(SearchQuery("", false))
    private val filteredSearchQueryFlow = searchQueryFlow.transformLatest { searchQuery ->
        val skipSearch = (actualSearchText == searchQuery.searchValue)
        if (searchQuery.searchValue.length >= Constant.MinimumCharForSearch) {
            // Add delay if user is taping
            if (!searchQuery.isFinalResearch) {
                delay(Constant.DelayBeforeSearch)
            }

            // Skip search if user already have result on screen
            if (!skipSearch) {
                actualSearchText = searchQuery.searchValue
                emit(searchQuery.searchValue)
            }
        } else {
            actualSearchText = searchQuery.searchValue
            emit(null)
        }
    }

    val searchResultFlow: Flow<LinkedHashSet<SafeItemWithIdentifier>?> =
        searchIndexManager.decryptedIndex
            .filterIsInstance<LBFlowResult.Success<List<PlainIndexWordEntry>>>()
            .combine(filteredSearchQueryFlow) { decryptedIndex, searchValue ->
                Pair(decryptedIndex, searchValue)
            }.flatMapLatest { (decryptedIndex, searchValue) ->
                searchValue?.let {
                    getMatchFromSearchUseCase(searchValue, decryptedIndex.successData)
                } ?: flow<LinkedHashSet<SafeItemWithIdentifier>?> { emit(null) }
            }

    private var actualSearchText: String = ""

    /**
     * @param text The search query to match
     * @param isFinalQuery Indicates that the query should be execute without delay
     * @param scope Coroutine scope used to initialize the index
     *
     * @return true if the [searchResultFlow] will be update
     */
    operator fun invoke(text: String, isFinalQuery: Boolean, scope: CoroutineScope): Boolean {
        val oldText = searchQueryFlow.value.searchValue
        searchIndexManager.initStoreIndex(scope)
        searchQueryFlow.value = SearchQuery(text, isFinalQuery)
        return oldText != text && text.length >= Constant.MinimumCharForSearch
    }
}
