package studio.lunabee.onesafe.feature.search.delegate

import kotlinx.coroutines.flow.StateFlow
import studio.lunabee.onesafe.feature.search.holder.SearchUiState

interface SearchLogicDelegate {
    val searchState: StateFlow<SearchUiState>
    val searchTextValue: StateFlow<String>
    fun initSearchIndex()
    fun search(text: String, finalQuery: Boolean)

    fun clickOnRecentSearch(recentSearch: String)

    fun saveActualSearchRecentSearch()
}
