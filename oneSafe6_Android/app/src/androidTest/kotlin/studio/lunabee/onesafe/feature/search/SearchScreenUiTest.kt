package studio.lunabee.onesafe.feature.search

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.lunabee.lbextensions.lazyFast
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbViewModel
import studio.lunabee.onesafe.feature.search.holder.SearchData
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.feature.search.holder.SearchUiState
import studio.lunabee.onesafe.feature.search.screen.SearchBottomSheet
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class SearchScreenUiTest : LbcComposeTest() {
    private val emptyIdleScreenText by lazyFast { getString(OSString.searchScreen_recent_emptyText) }
    private val recentItemsIdleScreen by lazyFast { getString(OSString.searchScreen_recent_items_title) }
    private val recentSearchIdleScreen by lazyFast { getString(OSString.searchScreen_recent_search_title) }
    private val resultSearchNoItemResultText by lazyFast { getString(OSString.searchScreen_result_noItem) }
    private val resultSearchNoResultText by lazyFast { getString(OSString.searchScreen_result_noResult_description) }
    private val resultSearchResultRes = OSString.searchScreen_result_title

    @Test
    fun search_empty_idle_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(
                    searchData = SearchData(),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithText(emptyIdleScreenText).assertExists()
        }
    }

    @Test
    fun search_filled_recent_items_idle_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(
                    searchData = SearchData(
                        recentItem = listOf(
                            AppAndroidTestUtils.createPlainItemData(itemNameProvider = DefaultNameProvider("item")),
                        ),
                    ),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithText(emptyIdleScreenText).assertDoesNotExist()
            onNodeWithText(recentItemsIdleScreen).assertExists()
            onNodeWithText("item").assertExists()
            onNodeWithText(recentSearchIdleScreen).assertDoesNotExist()
        }
    }

    @Test
    fun search_filled_recent_search_idle_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(
                    searchData = SearchData(
                        recentSearch = listOf("Test", "Test2"),
                    ),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithText(emptyIdleScreenText).assertDoesNotExist()
            onNodeWithText(recentItemsIdleScreen).assertDoesNotExist()
            onNodeWithText(recentSearchIdleScreen).assertExists()
            onNodeWithText("Test").assertExists()
            onNodeWithText("Test2").assertExists()
        }
    }

    @Test
    fun search_filled_idle_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(
                    searchData = SearchData(
                        recentSearch = listOf("Test", "Test2"),
                        recentItem = listOf(
                            AppAndroidTestUtils.createPlainItemData(itemNameProvider = DefaultNameProvider("item")),
                        ),
                    ),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithText(emptyIdleScreenText).assertDoesNotExist()
            onNodeWithText(recentItemsIdleScreen).assertExists()
            onNodeWithText(recentSearchIdleScreen).assertExists()
            onNodeWithText("Test").assertExists()
            onNodeWithText("Test2").assertExists()
            onNodeWithText("item").assertExists()
        }
    }

    @Test
    fun search_loading_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = true,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(searchData = SearchData()),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.SearchLoading).assertIsDisplayed()
        }
    }

    @Test
    fun search_no_loading_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Idle(searchData = SearchData()),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithTag(UiConstants.TestTag.Screen.SearchScreen).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.SearchLoading).assertDoesNotExist()
        }
    }

    @Test
    fun search_no_result_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 10,
                resultUIState = SearchResultUiState.Searching(
                    result = null,
                    deletedResult = null,
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithText(resultSearchNoResultText).assertIsDisplayed()
        }
    }

    @Test
    fun search_no_result_no_item_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Searching(
                    result = null,
                    deletedResult = null,
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithText(resultSearchNoItemResultText).assertIsDisplayed()
        }
    }

    @Test
    fun search_result_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Searching(
                    result = listOf(
                        AppAndroidTestUtils.createPlainItemDataRow(
                            itemNameProvider = DefaultNameProvider("item"),
                            identifier = "Identifier",
                        ),
                    ),
                    deletedResult = listOf(),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithText(resultSearchNoResultText).assertDoesNotExist()
            onNodeWithText(getString(resultSearchResultRes, 1)).assertIsDisplayed()
            onNodeWithText(getString(OSString.searchScreen_result_bin_title, 1)).assertDoesNotExist()
            hasText("item Identifier") // onNodeWithText will use the accessibility description
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
            hasText("Identifier")
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun search_deleted_result_screen() {
        val vm = mockk<BreadcrumbViewModel>()
        every { vm.searchState } returns MutableStateFlow(
            SearchUiState(
                isLoading = false,
                itemCount = 0,
                resultUIState = SearchResultUiState.Searching(
                    result = null,
                    deletedResult = listOf(
                        AppAndroidTestUtils.createPlainItemDataRow(
                            itemNameProvider = DefaultNameProvider("item"),
                            identifier = "Identifier",
                        ),
                    ),
                ),
            ),
        )
        setSearchScreen(vm) {
            onNodeWithText(resultSearchNoResultText).assertDoesNotExist()
            onNodeWithText(getString(OSString.searchScreen_result_bin_title, 1)).assertIsDisplayed()
        }
    }

    private fun setSearchScreen(viewModel: BreadcrumbViewModel, block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                OSTheme {
                    val searchState by viewModel.searchState.collectAsState()
                    SearchBottomSheet(
                        isVisible = true,
                        onBottomSheetClosed = {},
                        searchState = searchState,
                        onRecentSearchClick = { },
                        onValueSearchChange = { _, _ -> },
                        searchValue = "",
                    ) { }
                }
            }
            block()
        }
    }
}
