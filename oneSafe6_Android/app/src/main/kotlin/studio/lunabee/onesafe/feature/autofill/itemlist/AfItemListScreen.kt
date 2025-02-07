package studio.lunabee.onesafe.feature.autofill.itemlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.lunabee.lbloading.LoadingBackHandler
import kotlinx.coroutines.flow.Flow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.common.utils.LazyItemPagedGrid
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.search.composable.DefaultSearchTextFieldContainer
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.feature.search.screen.searchResultScreen
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

@Composable
fun AfItemListRoute(
    navigateBack: () -> Unit,
    onCredentialProvided: (identifier: String, password: String) -> Unit,
    viewModel: AfItemListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val itemsDataState by viewModel.itemsState.collectAsStateWithLifecycle()
    val searchValue by viewModel.searchTextValue.collectAsStateWithLifecycle()

    LoadingBackHandler(enabled = true) {
        navigateBack()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (uiState) {
            is AfItemListScreenUiState.Data -> {
                val state = uiState as AfItemListScreenUiState.Data
                AutoFillItemListScreen(
                    navigateBack = navigateBack,
                    state = itemsDataState,
                    onClickOnItem = viewModel::findItemIdentifierAndPassword,
                    onSearchValueChange = viewModel::autofillSearch,
                    searchValue = searchValue,
                    isSearchLoading = state.isLoading,
                    itemsCount = state.itemCount,
                )
            }

            is AfItemListScreenUiState.Exit -> {
                val state = uiState as AfItemListScreenUiState.Exit
                onCredentialProvided(state.identifier, state.password)
            }
        }
    }
}

@Composable
fun AutoFillItemListScreen(
    navigateBack: () -> Unit,
    state: AfItemDataState,
    searchValue: String,
    onSearchValueChange: (String, Boolean) -> Unit,
    onClickOnItem: (id: UUID) -> Unit,
    isSearchLoading: Boolean,
    itemsCount: Int,
) {
    val focusRequester = remember { FocusRequester() }
    val localFocusManager = LocalFocusManager.current

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = searchValue,
                selection = TextRange(searchValue.length),
            ),
        )
    }
    OSScreen(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        testTag = UiConstants.TestTag.Screen.AutofillItemsListScreen,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OSTopAppBar(
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )

            if (itemsCount > 0) {
                DefaultSearchTextFieldContainer(
                    textFieldValue = textFieldValueState,
                    focusRequester = focusRequester,
                    focusManager = localFocusManager,
                    itemCount = itemsCount,
                    onValueChange = { textFieldValue, finalSearch ->
                        textFieldValueState = textFieldValue
                        onSearchValueChange(textFieldValue.text, finalSearch)
                    },
                    onClear = {
                        focusRequester.requestFocus()
                        textFieldValueState = TextFieldValue(text = "")
                        onSearchValueChange("", false)
                    },
                )
                if (isSearchLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(UiConstants.TestTag.Item.SearchLoading),
                    )
                } else {
                    Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.ExtraSmall))
                }
            }

            when (state) {
                is AfItemDataState.AllItems -> {
                    AllItemsListContent(
                        items = state.items,
                        suggestedItems = state.suggestedItems,
                        onClickOnItem = onClickOnItem,
                    )
                }

                is AfItemDataState.Searching -> {
                    SearchingItemsContent(
                        items = state.items,
                        onClickOnItem = onClickOnItem,
                        allItemCount = state.allItemsCount,
                    )
                }

                is AfItemDataState.NoItem -> {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(OSString.searchScreen_result_noItem),
                        modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                    )
                }
            }
        }
    }
}

@Composable
private fun AllItemsListContent(
    items: Flow<PagingData<PlainItemDataRow>>,
    suggestedItems: List<PlainItemDataRow>,
    onClickOnItem: (UUID) -> Unit,
) {
    val safeItems: LazyPagingItems<PlainItemDataRow> = items.collectAsLazyPagingItems()
    val showOtherItemsLoading: Boolean by LazyItemPagedGrid.rememberShowDelayedLoading(safeItems)

    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
    ) {
        if (suggestedItems.isNotEmpty()) {
            AfItemListFactory.addSafeItems(
                safeItems = suggestedItems,
                onClickOnItem = onClickOnItem,
                header = LbcTextSpec.StringResource(OSString.autofill_safeItemsList_suggestionsHeader),
                headerKey = AfItemListFactory.SuggestionHeaderKey,
            )
        }

        if (suggestedItems.isNotEmpty() && (safeItems.itemCount > 0 || showOtherItemsLoading)) {
            lazyVerticalOSRegularSpacer()
        }

        if (showOtherItemsLoading) {
            AfItemListFactory.addShimmersItems(this)
        } else {
            AfItemListFactory.addPaginatedSafeItems(
                safeItems = safeItems,
                onClickOnItem = onClickOnItem,
                showHeader = suggestedItems.isNotEmpty() && safeItems.itemCount > 0,
            )
        }
    }
    val localFocusManager = LocalFocusManager.current
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            localFocusManager.clearFocus()
        }
    }
}

@Composable
private fun SearchingItemsContent(
    items: List<PlainItemDataRow>,
    allItemCount: Int,
    onClickOnItem: (UUID) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
    ) {
        searchResultScreen(
            itemCount = allItemCount,
            state = SearchResultUiState.Searching(
                result = items,
                deletedResult = null,
            ),
            onItemClick = onClickOnItem,
        )
    }
    val localFocusManager = LocalFocusManager.current
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            localFocusManager.clearFocus()
        }
    }
}
