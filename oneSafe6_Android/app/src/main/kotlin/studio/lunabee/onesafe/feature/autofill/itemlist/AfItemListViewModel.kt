package studio.lunabee.onesafe.feature.autofill.itemlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.model.item.toPlainItemDataRow
import studio.lunabee.onesafe.domain.model.client.AfClientData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetAllSafeItemsWithIdentifierUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemWithIdentifierCountUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.search.SearchItemUseCase
import studio.lunabee.onesafe.feature.autofill.delegate.AfGetClientDataDelegate
import studio.lunabee.onesafe.feature.autofill.delegate.AfGetClientDataDelegateImpl
import studio.lunabee.onesafe.feature.search.delegate.SearchLogicDelegate
import studio.lunabee.onesafe.feature.search.delegate.SearchLogicDelegateImpl
import studio.lunabee.onesafe.jvm.mapPagingValues
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AfItemListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchLogicDelegateImpl: SearchLogicDelegateImpl,
    private val afGetClientDataDelegateImpl: AfGetClientDataDelegateImpl,
    val safeItemRepository: SafeItemRepository,
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val searchItemUseCase: SearchItemUseCase,
    private val getItemWithIdentifierCountUseCase: GetItemWithIdentifierCountUseCase,
    private val getAllSafeItemsWithIdentifierUseCase: GetAllSafeItemsWithIdentifierUseCase,
    getIconUseCase: GetIconUseCase,
) : ViewModel(),
    SearchLogicDelegate by searchLogicDelegateImpl,
    AfGetClientDataDelegate by afGetClientDataDelegateImpl {

    private val clientDomain: String = savedStateHandle.get<String>(AfItemListDestination.ClientDomainArgs).orEmpty()
    private val clientPackage: String = savedStateHandle.get<String>(AfItemListDestination.ClientPackageArgs).orEmpty()

    private val _uiState: MutableStateFlow<AfItemListScreenUiState> = MutableStateFlow(AfItemListScreenUiState.Idle)
    val uiState: StateFlow<AfItemListScreenUiState> = _uiState

    private val _itemsState: MutableStateFlow<AfItemDataState> = MutableStateFlow(AfItemDataState.Idle)
    val itemsState: StateFlow<AfItemDataState> = _itemsState

    private val _searchTextValue: MutableStateFlow<String> = MutableStateFlow("")
    override val searchTextValue: StateFlow<String> = _searchTextValue.asStateFlow()

    init {
        searchLogicDelegateImpl.initSearchIndex()
        viewModelScope.launch {
            // If we have data for the specific client, we fetch it and use the search to display it as a suggestion.
            val clientData: AfClientData? = getClientData(clientDomain, clientPackage)
            val query = clientData?.name?.takeIf { it.isNotEmpty() }
                ?: clientDomain.takeIf { it.isNotEmpty() }
                ?: clientPackage
            searchItemUseCase(
                query,
                true,
                this,
            )
            val suggestions = searchItemUseCase.searchResultFlow.first() ?: listOf()

            val safeItemsFlow = getAllSafeItemsWithIdentifierUseCase(
                pagingConfig = AppConstants.Pagination.DefaultPagingConfig,
                suggestions = suggestions,
            ).mapPagingValues { item ->
                item.toPlainItemDataRow(decryptUseCase, getIconUseCase) { { listOf() } }
            }.cachedIn(viewModelScope)

            combine(
                getItemWithIdentifierCountUseCase(),
                searchItemUseCase
                    .searchResultFlow
                    .onStart { emit(null) },
            ) { itemCount, searchResult ->
                _uiState.value = AfItemListScreenUiState.Data(
                    isLoading = false,
                    itemCount = itemCount,
                )
                itemCount to searchResult
            }.distinctUntilChanged().collect { result ->
                yield() // Fix weird state behavior, probably due to UI thread busy here while searchState is being update
                _itemsState.value = if (result.second != null) {
                    AfItemDataState.Searching(
                        items = result.second!!.map { it.toPlainItemDataRow(decryptUseCase, getIconUseCase) { { listOf() } } },
                        allItemsCount = result.first,
                    )
                } else if (result.first == 0) {
                    AfItemDataState.NoItem
                } else {
                    AfItemDataState.AllItems(
                        suggestions.map { it.toPlainItemDataRow(decryptUseCase, getIconUseCase) { { listOf() } } },
                        safeItemsFlow,
                    )
                }
            }
        }
    }

    fun findItemIdentifierAndPassword(itemId: UUID) {
        viewModelScope.launch {
            val fields = safeItemFieldRepository.getSafeItemFields(itemId)
            var identifier = ""
            fields.firstOrNull { it.isItemIdentifier }?.encValue?.let {
                val result = decryptUseCase(it, itemId, String::class)
                if (result is LBResult.Success) {
                    identifier = result.data.orEmpty()
                }
            }

            var password = ""
            fields.firstOrNull {
                it.encKind != null &&
                    decryptUseCase(it.encKind as ByteArray, itemId, SafeItemFieldKind::class).data == SafeItemFieldKind.Password
            }?.encValue?.let {
                val result = decryptUseCase(it, itemId, String::class)
                if (result is LBResult.Success) {
                    password = result.data.orEmpty()
                }
            }

            _uiState.value = AfItemListScreenUiState.Exit(identifier, password)
        }
    }

    fun autofillSearch(text: String, finalQuery: Boolean) {
        _searchTextValue.value = text
        viewModelScope.launch {
            (_uiState.value as? AfItemListScreenUiState.Data)?.let {
                _uiState.value = it.copy(
                    isLoading = searchItemUseCase(text, finalQuery, this),
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchLogicDelegateImpl.close()
    }
}
