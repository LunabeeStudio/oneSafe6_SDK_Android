package studio.lunabee.onesafe.feature.search

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbextensions.lazyFast
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import studio.lunabee.onesafe.AppUnitTestUtils
import studio.lunabee.onesafe.common.model.item.toPlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.toPlainItemDataRow
import studio.lunabee.onesafe.domain.manager.SearchIndexManager
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemCountUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.SecureGetRecentItemUseCase
import studio.lunabee.onesafe.domain.usecase.search.EncryptAndSaveRecentSearchUseCase
import studio.lunabee.onesafe.domain.usecase.search.SearchItemUseCase
import studio.lunabee.onesafe.domain.usecase.search.SecureGetRecentSearchUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.search.delegate.SearchLogicDelegateImpl
import studio.lunabee.onesafe.feature.search.holder.SearchResultUiState
import studio.lunabee.onesafe.test.OSUiThreadTest
import studio.lunabee.onesafe.test.firstSafeId
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SearchLogicDelegateTest : OSUiThreadTest() {
    private val searchLogicDelegateImpl: SearchLogicDelegateImpl by lazyFast {
        SearchLogicDelegateImpl(
            decryptUseCase = decryptUseCase,
            getIconUseCase = getIconUseCase,
            searchItemUseCase = searchItemUseCase,
            secureGetRecentItemUseCase = getRecentItemUseCase,
            secureGetRecentSearchUseCase = getRecentSearchUseCase,
            getItemCountUseCase = getItemCountUseCase,
            searchIndexManager = indexManager,
            encryptAndSaveRecentSearchUseCase = encryptAndSaveRecentSearchUseCase,
            getSafeItemActionHelper = getSafeItemActionHelper,
        )
    }

    @MockK lateinit var indexManager: SearchIndexManager

    @MockK lateinit var decryptUseCase: ItemDecryptUseCase

    @MockK lateinit var getIconUseCase: GetIconUseCase

    @MockK lateinit var searchItemUseCase: SearchItemUseCase

    @MockK lateinit var getRecentSearchUseCase: SecureGetRecentSearchUseCase

    @MockK lateinit var getRecentItemUseCase: SecureGetRecentItemUseCase

    @MockK lateinit var getItemCountUseCase: GetItemCountUseCase

    @MockK lateinit var indexWordEntryRepository: IndexWordEntryRepository

    @MockK lateinit var encryptAndSaveRecentSearchUseCase: EncryptAndSaveRecentSearchUseCase

    @MockK lateinit var getSafeItemActionHelper: GetSafeItemActionHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { getRecentItemUseCase.invoke() } returns flowOf(emptyList())
        every { getRecentSearchUseCase.invoke() } returns flowOf(LBFlowResult.Loading())
        every { getItemCountUseCase.invoke() } returns flowOf(0)
        every { searchItemUseCase.searchResultFlow } returns flowOf(null)
        every { indexWordEntryRepository.getAll(firstSafeId) } returns flowOf(emptyList())
        coEvery { getSafeItemActionHelper.getQuickActions(any(), any()) } returns { listOf() }
    }

    @Test
    fun initSearch_idle_empty_test(): TestResult = runTest {
        searchLogicDelegateImpl.initSearch()

        val resultUIState = searchLogicDelegateImpl.searchState.value.resultUIState
        assertIs<SearchResultUiState.Idle>(resultUIState)
        assertContentEquals(emptyList(), resultUIState.searchData.recentSearch)
        assertContentEquals(emptyList(), resultUIState.searchData.recentItem)
        assertEquals(0, searchLogicDelegateImpl.searchState.value.itemCount)
        assertFalse(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun initSearch_idle_recent_item_test(): TestResult = runTest {
        mockkStatic(SafeItem::toPlainItemDataDefault)
        val itemPagination = AppUnitTestUtils.createPlainItemDataDefault()
        val safeItem = mockk<SafeItem> {
            every { id } returns UUID.randomUUID()
            coEvery { toPlainItemDataDefault(any(), any(), any()) } returns itemPagination
        }
        every { getRecentItemUseCase.invoke() } returns flowOf(listOf(safeItem))

        searchLogicDelegateImpl.initSearch()

        val resultUIState = searchLogicDelegateImpl.searchState.value.resultUIState
        assertIs<SearchResultUiState.Idle>(resultUIState)
        assertContentEquals(emptyList(), resultUIState.searchData.recentSearch)
        assertContentEquals(listOf(itemPagination), resultUIState.searchData.recentItem)
        assertEquals(0, searchLogicDelegateImpl.searchState.value.itemCount)
        assertFalse(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun initSearch_idle_recent_search_test(): TestResult = runTest {
        every { getRecentSearchUseCase.invoke() } returns flowOf(LBFlowResult.Success(listOf("aaa")))

        searchLogicDelegateImpl.initSearch()

        val resultUIState = searchLogicDelegateImpl.searchState.value.resultUIState
        assertIs<SearchResultUiState.Idle>(resultUIState)
        assertContentEquals(listOf("aaa"), resultUIState.searchData.recentSearch)
        assertContentEquals(emptyList(), resultUIState.searchData.recentItem)
        assertEquals(0, searchLogicDelegateImpl.searchState.value.itemCount)
        assertFalse(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun initSearch_idle_items_test(): TestResult = runTest {
        every { getItemCountUseCase.invoke() } returns flowOf(10)

        searchLogicDelegateImpl.initSearch()

        val resultUIState = searchLogicDelegateImpl.searchState.value.resultUIState
        assertIs<SearchResultUiState.Idle>(resultUIState)
        assertContentEquals(emptyList(), resultUIState.searchData.recentSearch)
        assertContentEquals(emptyList(), resultUIState.searchData.recentItem)
        assertEquals(10, searchLogicDelegateImpl.searchState.value.itemCount)
        assertFalse(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun search_loading_test(): TestResult = runTest {
        every { searchItemUseCase.invoke("aaa", true, any()) } returns true

        searchLogicDelegateImpl.initSearch()
        searchLogicDelegateImpl.search("aaa", true)

        assertEquals(0, searchLogicDelegateImpl.searchState.value.itemCount)
        assertTrue(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("aaa", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun search_no_loading_test(): TestResult = runTest {
        every { searchItemUseCase.invoke("aaa", true, any()) } returns false

        searchLogicDelegateImpl.initSearch()
        searchLogicDelegateImpl.search("aaa", true)

        assertFalse(searchLogicDelegateImpl.searchState.value.isLoading)
        assertEquals("aaa", searchLogicDelegateImpl.searchTextValue.value)
    }

    @Test
    fun initSearch_searching_test(): TestResult = runTest {
        mockkStatic(SafeItemWithIdentifier::toPlainItemDataRow)
        val itemPagination = AppUnitTestUtils.createPlainItemDataRow()
        val searchItem = mockk<SafeItemWithIdentifier> {
            every { isDeleted } returns false
            coEvery { toPlainItemDataRow(any(), any(), any()) } returns itemPagination
        }
        every { searchItemUseCase.searchResultFlow } returns flowOf(listOf(searchItem))

        searchLogicDelegateImpl.initSearch()

        val resultUIState = searchLogicDelegateImpl.searchState.value.resultUIState
        assertIs<SearchResultUiState.Searching>(resultUIState)
        assertContentEquals(listOf(itemPagination), resultUIState.result)
    }
}
