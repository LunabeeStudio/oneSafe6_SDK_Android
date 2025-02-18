package studio.lunabee.onesafe.feature.search.delegate

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault.Companion.mapForUi
import studio.lunabee.onesafe.common.model.item.toPlainItemDataRow
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.manager.SearchIndexManager
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemCountUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetRecentItemUseCase
import studio.lunabee.onesafe.domain.usecase.search.EncryptAndSaveRecentSearchUseCase
import studio.lunabee.onesafe.domain.usecase.search.SearchItemUseCase
import studio.lunabee.onesafe.domain.usecase.search.SecureGetRecentSearchUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import studio.lunabee.onesafe.feature.search.holder.SearchData
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.feature.search.holder.SearchUiState
import javax.inject.Inject

class SearchLogicDelegateImpl @Inject constructor(
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val searchItemUseCase: SearchItemUseCase,
    private val secureGetRecentItemUseCase: SecureGetRecentItemUseCase,
    private val secureGetRecentSearchUseCase: SecureGetRecentSearchUseCase,
    private val getItemCountUseCase: GetItemCountUseCase,
    private val searchIndexManager: SearchIndexManager,
    private val encryptAndSaveRecentSearchUseCase: EncryptAndSaveRecentSearchUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper,
) : SearchLogicDelegate,
    CloseableCoroutineScope by CloseableMainCoroutineScope(),
    GetSafeItemActionUiStateDelegate by getSafeItemActionHelper {

    private val _searchState = MutableStateFlow(SearchUiState(isLoading = true, itemCount = 0))
    override val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _searchTextValue: MutableStateFlow<String> = MutableStateFlow("")
    override val searchTextValue: StateFlow<String> = _searchTextValue.asStateFlow()
    private var searchData: SearchData = SearchData()

    suspend fun initSearch() {
        combine(
            secureGetRecentItemUseCase().mapForUi(
                decryptUseCase,
                getIconUseCase,
                getQuickAction = getSafeItemActionHelper::getQuickActions,
            ),
            secureGetRecentSearchUseCase(),
            getItemCountUseCase(),
            searchItemUseCase.searchResultFlow
                .onStart { emit(null) }, // don't wait searchResultFlow to init
        ) { items, search, itemCount, searchResult ->
            // We cut the loading here before the distinct until change if the search result did not change.
            _searchState.value = searchState.value.copy(isLoading = false)
            val recentSearch = search.data.orEmpty() // no loading nor error (log in use case)
            SearchResultData(items, recentSearch, itemCount, searchResult)
        }
            .distinctUntilChanged()
            .collect { searchResultData ->
                yield() // Fix weird state behavior, probably due to UI thread busy here while searchState is being update
                searchData = SearchData(
                    recentItem = searchResultData.recentItems,
                    recentSearch = searchResultData.recentSearch,
                )
                _searchState.value = if (searchResultData.searchResult != null) {
                    val resultMap = searchResultData.searchResult
                        .groupBy { it.isDeleted }
                        .mapValues { (_, items) ->
                            items.mapNotNull { item ->
                                item.toPlainItemDataRow(
                                    decryptUseCase,
                                    getIconUseCase,
                                    getSafeItemActionHelper::getQuickActions,
                                ).takeIf {
                                    // Check if item is decipherable (might not be due to item deletion + search cache)
                                    it.itemNameProvider !is ErrorNameProvider
                                }
                            }
                        }
                    searchState.value.copy(
                        itemCount = searchResultData.itemCount,
                        isLoading = false,
                        resultUIState = SearchResultUiState.Searching(
                            result = resultMap[false],
                            deletedResult = resultMap[true],
                        ),
                    )
                } else {
                    searchState.value.copy(
                        itemCount = searchResultData.itemCount,
                        isLoading = false,
                        resultUIState = SearchResultUiState.Idle(
                            searchData = searchData,
                        ),
                    )
                }
            }
    }

    override fun initSearchIndex() {
        searchIndexManager.initStoreIndex(coroutineScope)
    }

    override fun search(text: String, finalQuery: Boolean) {
        _searchTextValue.value = text
        if (searchItemUseCase(text.trim(), finalQuery, coroutineScope)) {
            _searchState.value = searchState.value.copy(isLoading = true)
        }
    }

    override fun saveActualSearchRecentSearch() {
        if (searchTextValue.value.isNotBlank()) {
            coroutineScope.launch {
                updateRecentSearch(searchTextValue.value.trim())
            }
        }
    }

    private suspend fun updateRecentSearch(newSearch: String) {
        encryptAndSaveRecentSearchUseCase(newSearch)
    }

    override fun clickOnRecentSearch(recentSearch: String) {
        coroutineScope.launch {
            search(recentSearch, true)
            updateRecentSearch(recentSearch)
        }
    }

    override fun close() {
        coroutineScope.cancel()
        getSafeItemActionHelper.close()
    }
}

data class SearchResultData(
    val recentItems: List<PlainItemData>,
    val recentSearch: List<String>,
    val itemCount: Int,
    val searchResult: List<SafeItemWithIdentifier>?,
)
