package studio.lunabee.onesafe.feature.autofill

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.HiltComponentActivity
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemDataState
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemListRoute
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemListScreenUiState
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemListViewModel
import studio.lunabee.onesafe.ui.theme.OSTheme

@HiltAndroidTest
class AfItemListScreenTest {

    @get:Rule(order = 0)
    var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<HiltComponentActivity>, HiltComponentActivity> =
        createAndroidComposeRule()

    private val safeItemRepository: SafeItemRepository = mockk()
    private val onCredentialProvided: (String, String) -> Unit = spyk({ _, _ -> })

    private val mockkVM: AfItemListViewModel = mockk()

    @Before
    fun setUp() {
        hiltRule.inject()
        every { mockkVM.itemsState } returns MutableStateFlow(
            AfItemDataState.AllItems(
                suggestedItems = listOf(),
                items = flowOf(PagingData.from(safeItemPaginationList)),
            ),
        )
        every { mockkVM.safeItemRepository } returns safeItemRepository
        every { mockkVM.searchTextValue } returns MutableStateFlow("")
    }

    private val mockItemsListSize: Int = 20
    val safeItemPaginationList: List<PlainItemDataRow> = AppAndroidTestUtils.createPlainItemDataRow(
        size = mockItemsListSize,
        itemNameProvider = { DefaultNameProvider("$it") },
    )

    /**
     * Test that if no items the search edit text is not displayed and we have a "no item" indication.
     */
    @Test
    fun no_item_state_test() {
        every { mockkVM.uiState } returns MutableStateFlow(AfItemListScreenUiState.Data(false, 0))
        every { mockkVM.itemsState } returns MutableStateFlow(AfItemDataState.NoItem)
        setScreen(mockkVM)
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(OSString.searchScreen_result_noItem)).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getQuantityString(OSPlurals.searchScreen_search_placeholder, 0, 0))
            .assertDoesNotExist()
    }

    /**
     * Test that when there is items, the search edit text is displayed.
     */
    @Test
    fun all_item_state_test() {
        every { mockkVM.uiState } returns MutableStateFlow(AfItemListScreenUiState.Data(false, mockItemsListSize))
        setScreen(mockkVM)
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(OSString.searchScreen_result_noItem)).assertDoesNotExist()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.resources
                    .getQuantityString(OSPlurals.searchScreen_search_placeholder, mockItemsListSize, mockItemsListSize),
            )
            .assertIsDisplayed()
    }

    /**
     * Test that once in "Exit" state, the onCredentialProvided is triggered
     */
    @Test
    fun test_exit_state_triggers_on_credential_provided() {
        val identifier = "identifier"
        val password = "password"
        every { mockkVM.uiState } returns MutableStateFlow(AfItemListScreenUiState.Exit(identifier, password))
        setScreen(mockkVM)
        verify(exactly = 1) { onCredentialProvided(identifier, password) }
    }

    private fun setScreen(viewModel: AfItemListViewModel) {
        composeTestRule.setContent {
            OSTheme {
                AfItemListRoute(
                    navigateBack = {},
                    viewModel = viewModel,
                    onCredentialProvided = onCredentialProvided,
                )
            }
        }
    }
}
