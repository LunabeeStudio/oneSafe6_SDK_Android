package studio.lunabee.onesafe.feature.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilNodeCount
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.AppAndroidTestUtils.loadedPagingStates
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.commonui.settings.ItemStyleHolder
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavigation
import studio.lunabee.onesafe.feature.home.model.HomeConversationSectionData
import studio.lunabee.onesafe.feature.home.model.HomeInfoSectionData
import studio.lunabee.onesafe.feature.home.model.ItemRowData
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionsBottomSheet
import studio.lunabee.onesafe.feature.supportus.SupportUsHomeInfoData
import studio.lunabee.onesafe.importexport.ui.AutoBackupErrorHomeInfoData
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest : LbcComposeTest() {

    private val homeScreenUiState = mockUiState()
    private val uiStateFlow = MutableStateFlow(homeScreenUiState)

    private val vm: HomeScreenViewModel = mockk {
        every { uiState } returns uiStateFlow
        every { navigationAction } returns flowOf(null)
        every { itemActionDialogState } returns MutableStateFlow(null)
        every { itemActionSnackbarState } returns MutableStateFlow(null)
        every { areItemsBeingGenerated } returns MutableStateFlow(false)
        every { snackbarState } returns MutableStateFlow(null)
        every { osFeatureFlags.bubbles() } returns true
        every { shouldAskForTranslationSupport } returns MutableStateFlow(false)
        every { hasPreventionWarnings } returns MutableStateFlow(false)
        every { shouldAskForTranslationSupport(any()) } returns Unit
        every { homeInfoSectionData } returns MutableStateFlow(HomeInfoSectionData(emptyList()))
        every { itemDisplayOptionsBottomSheet } returns MutableStateFlow(
            ItemDisplayOptionsBottomSheet(
                onSelectItemOrder = {},
                selectedItemOrder = ItemOrder.Alphabetic,
                onSelectItemLayout = {},
                selectedItemLayout = OSTestConfig.itemLayouts,
            ),
        )
        every { homeConversationSection } returns MutableStateFlow(HomeConversationSectionData(emptyList()))
    }

    @Test
    fun home_empty_state() {
        every { vm.uiState } returns MutableStateFlow(
            HomeScreenUiState(
                items = flowOf(
                    PagingData.empty(
                        loadedPagingStates(),
                    ),
                ),
                initialItemCount = 0,
                favoriteItems = flowOf(persistentListOf()),
                deletedItemCount = 0,
                isBiometricEnabled = false,
                isLanguageGenerated = false,
                showFavoritesSeeAll = false,
                isAppBetaVersion = false,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
        )

        setHomeScreen {
            // Home screen
            onRoot().printToCacheDir(printRule)
            onNodeWithTag(UiConstants.TestTag.Screen.Home).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid).assertDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.Item.DiscoveryItemCard).assertIsDisplayed()
        }
    }

    @Test
    fun home_item_filled_state() {
        val itemSize = 100
        val lastIndex = itemSize - 1
        val testSuffix = "_test"

        val safeItemPaginationList = AppAndroidTestUtils.createPlainItemData(
            size = itemSize,
            itemNameProvider = { DefaultNameProvider("${it}$testSuffix") },
            identifier = { LbcTextSpec.Raw("identifier_${it}$testSuffix") },
        )

        every { vm.uiState } returns MutableStateFlow(
            HomeScreenUiState(
                items = flowOf(
                    PagingData.from(
                        data = safeItemPaginationList,
                        sourceLoadStates = loadedPagingStates(),
                    ),
                ),
                initialItemCount = itemSize,
                favoriteItems = flowOf(persistentListOf()),
                deletedItemCount = 0,
                isBiometricEnabled = false,
                isLanguageGenerated = false,
                showFavoritesSeeAll = false,
                isAppBetaVersion = false,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
        )

        setHomeScreen {
            // Home screen
            val rootNode = onRoot()
            rootNode.printToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}_top")
            onNodeWithTag(UiConstants.TestTag.Screen.Home).assertIsDisplayed()

            val gridNode = onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid)
            gridNode.assertIsDisplayed()

            // useUnmergedTree to handle both grid and list (list also have the identifier merged)
            val firstItemNode = onAllNodesWithText("0$testSuffix", useUnmergedTree = true)
            val lastItemNode = onAllNodesWithText("${lastIndex}$testSuffix", useUnmergedTree = true)

            firstItemNode.onFirst().isDisplayed()
            lastItemNode.assertCountEquals(0)

            gridNode
                .assert(hasScrollToIndexAction())
                .assert(hasScrollAction())
                .performScrollToIndex(lastIndex)

            rootNode.printToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}_bottom")

            firstItemNode.assertCountEquals(0)
            lastItemNode.onFirst().isDisplayed()

            onAllNodesWithText(text = testSuffix, substring = true)
                .assertAll(hasClickAction())
        }
    }

    @Test
    fun home_favorite_see_all_test() {
        val seeAllText = getString(OSString.common_seeAll)

        seeAllTest(seeAllText) { uiState ->
            every { uiState.showFavoritesSeeAll } returns true
            every { uiState.items } returns flowOf(
                PagingData.from(
                    listOf(AppAndroidTestUtils.createPlainItemData()),
                    loadedPagingStates(),
                ),
            )
        }
    }

    private fun seeAllTest(
        seeAllText: String,
        mock: (uiState: HomeScreenUiState) -> Unit,
    ) {
        mock(vm.uiState.value)

        setHomeScreen {
            onRoot().printToCacheDir(printRule)
            onAllNodesWithText(seeAllText)
                .onFirst()
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    fun home_favorite_filled_test() {
        val favoriteRowDataList = List(5) {
            ItemRowData.Item(AppAndroidTestUtils.createPlainItemData(itemNameProvider = DefaultNameProvider("test_$it")))
        }.toImmutableList()

        sectionFilledTest(favoriteRowDataList) { uiState ->
            every { uiState.favoriteItems } returns flowOf(favoriteRowDataList)
            every { uiState.items } returns flowOf(
                PagingData.from(
                    listOf(AppAndroidTestUtils.createPlainItemData()),
                    loadedPagingStates(),
                ),
            )
        }
    }

    @Test
    fun home_deleted_filled_test() {
        val itemCount = 5
        every { vm.uiState } returns MutableStateFlow(
            HomeScreenUiState(
                items = flowOf(
                    PagingData.empty(
                        loadedPagingStates(),
                    ),
                ),
                initialItemCount = 0,
                favoriteItems = flowOf(persistentListOf()),
                deletedItemCount = itemCount,
                isBiometricEnabled = true,
                isLanguageGenerated = false,
                showFavoritesSeeAll = false,
                isAppBetaVersion = false,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = false,
                isSafeReady = true,
            ),
        )

        setHomeScreen {
            // Home screen is empty even with deleted items
            onRoot().printToCacheDir(printRule)
            onNodeWithTag(UiConstants.TestTag.Screen.Home).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.HomeItemGrid).assertIsDisplayed()

            (0 until itemCount).map {
                onNodeWithText("test_$it").assertDoesNotExist()
            }
            onNodeWithText(getString(OSString.common_bin)).assertExists()
            onNodeWithText(itemCount.toString()).assertExists()
        }
    }

    @Test
    fun home_display_verify_password_action_according_to_biometric_test() {
        val itemCount = 5
        var uiState = HomeScreenUiState(
            items = flowOf(
                PagingData.empty(
                    loadedPagingStates(),
                ),
            ),
            initialItemCount = 5,
            favoriteItems = flowOf(persistentListOf()),
            deletedItemCount = itemCount,
            isBiometricEnabled = true,
            isLanguageGenerated = false,
            showFavoritesSeeAll = false,
            isAppBetaVersion = false,
            shouldVerifyPassword = false,
            hasDoneOnBoardingBubbles = false,
            isSafeReady = true,
        )
        every { vm.uiState } returns MutableStateFlow(
            uiState,
        )

        setHomeScreen {
            onNodeWithText(getString(OSString.home_verifyPassword_title)).performScrollTo().assertIsDisplayed()
        }

        uiState = HomeScreenUiState(
            items = flowOf(
                PagingData.empty(
                    loadedPagingStates(),
                ),
            ),
            initialItemCount = 5,
            favoriteItems = flowOf(persistentListOf()),
            deletedItemCount = itemCount,
            isBiometricEnabled = false,
            isLanguageGenerated = false,
            showFavoritesSeeAll = false,
            isAppBetaVersion = false,
            shouldVerifyPassword = false,
            hasDoneOnBoardingBubbles = false,
            isSafeReady = true,
        )
        every { vm.uiState } returns MutableStateFlow(
            uiState,
        )

        setHomeScreen {
            onNodeWithText(getString(OSString.home_verifyPassword_title)).assertDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.Item.SupportUsCard).assertDoesNotExist()
        }
    }

    @Test
    fun home_screen_display_support_and_error_cards_test() {
        val itemCount = 5
        val uiState = HomeScreenUiState(
            items = flowOf(
                PagingData.empty(
                    loadedPagingStates(),
                ),
            ),
            initialItemCount = 5,
            favoriteItems = flowOf(persistentListOf()),
            deletedItemCount = itemCount,
            isBiometricEnabled = true,
            isLanguageGenerated = false,
            showFavoritesSeeAll = false,
            isAppBetaVersion = false,
            shouldVerifyPassword = false,
            hasDoneOnBoardingBubbles = true,
            isSafeReady = true,
        )
        every { vm.uiState } returns MutableStateFlow(
            uiState,
        )
        every { vm.homeInfoSectionData } returns MutableStateFlow(
            HomeInfoSectionData(
                listOf(
                    SupportUsHomeInfoData(visibleSince = Instant.EPOCH, onDismiss = {}) {},
                    AutoBackupErrorHomeInfoData(
                        errorLabel = loremIpsumSpec(1),
                        errorFull = loremIpsumSpec(10),
                        visibleSince = Instant.EPOCH,
                    ) {},
                ),
            ),
        )

        setHomeScreen {
            onNodeWithTag(UiConstants.TestTag.Item.SupportUsCard).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.AutoBackupErrorCard).assertIsDisplayed()
        }
    }

    @Test
    fun home_screen_display_conversations_test() {
        val uiState = HomeScreenUiState(
            items = flowOf(
                PagingData.empty(
                    loadedPagingStates(),
                ),
            ),
            initialItemCount = 5,
            favoriteItems = flowOf(persistentListOf()),
            deletedItemCount = 5,
            isBiometricEnabled = true,
            isLanguageGenerated = false,
            showFavoritesSeeAll = false,
            isAppBetaVersion = false,
            shouldVerifyPassword = false,
            hasDoneOnBoardingBubbles = false,
            isSafeReady = true,
        )
        every { vm.uiState } returns MutableStateFlow(
            uiState,
        )
        every { vm.homeConversationSection } returns MutableStateFlow(
            HomeConversationSectionData(
                listOf(
                    BubblesConversationInfo(
                        id = DoubleRatchetUUID(UUID.randomUUID()),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = true,
                    ),
                    BubblesConversationInfo(
                        id = DoubleRatchetUUID(UUID.randomUUID()),
                        nameProvider = DefaultNameProvider(loremIpsum(1)),
                        subtitle = ConversationSubtitle.Message(loremIpsumSpec(4)),
                        hasUnreadMessage = false,
                    ),
                ),
            ),
        )

        setHomeScreen {
            hasTestTag(UiConstants.TestTag.Item.ConversationCard).waitUntilNodeCount(2)
        }
    }

    @Test
    fun home_item_order_bottom_sheet_test() {
        setHomeScreen {
            hasContentDescription(getString(OSString.home_displayOptions_sorting_contentDescription))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.ItemDisplayOptionsBottomSheet)
                .waitUntilExactlyOneExists()
        }
    }

    @Test
    fun home_item_appBetaVersion_test() {
        every { homeScreenUiState.isAppBetaVersion } returns true
        setHomeScreen {
            hasText(getString(OSString.appBetaVersion_chip))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }

        every { homeScreenUiState.isAppBetaVersion } returns false
        setHomeScreen {
            hasText(getString(OSString.appBetaVersion_chip))
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun home_item_show_translation_menu_test() {
        uiStateFlow.value = mockUiState(itemSize = 1) {
            every { isLanguageGenerated } returns true
        }

        setHomeScreen {
            hasText(getString(OSString.home_translate_menu_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            uiStateFlow.value = mockUiState(itemSize = 1) {
                every { isLanguageGenerated } returns false
            }
            hasText(getString(OSString.home_translate_menu_title))
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun home_bubbles_only_state() {
        val rawName = "rawName"
        val rawMessage = "rawMessage"
        every { vm.homeConversationSection } returns MutableStateFlow(
            HomeConversationSectionData(
                listOf(
                    BubblesConversationInfo(
                        DoubleRatchetUUID(testUUIDs[0]),
                        DefaultNameProvider(rawName),
                        ConversationSubtitle.Message(LbcTextSpec.Raw(rawMessage)),
                        false,
                    ),
                ),
            ),
        )
        every { vm.uiState } returns MutableStateFlow(
            HomeScreenUiState(
                items = flowOf(
                    PagingData.empty(
                        loadedPagingStates(),
                    ),
                ),
                initialItemCount = 0,
                favoriteItems = flowOf(persistentListOf()),
                deletedItemCount = 0,
                isBiometricEnabled = false,
                isLanguageGenerated = false,
                showFavoritesSeeAll = false,
                isAppBetaVersion = false,
                shouldVerifyPassword = false,
                hasDoneOnBoardingBubbles = true,
                isSafeReady = true,
            ),
        )

        setHomeScreen {
            hasTestTag(UiConstants.TestTag.Screen.Home).waitUntilExactlyOneExists().assertIsDisplayed()
            hasText(rawName).waitUntilExactlyOneExists().assertIsDisplayed()
            hasText(rawMessage).waitUntilExactlyOneExists().assertIsDisplayed()
        }
    }

    private fun sectionFilledTest(
        data: List<ItemRowData.Item>,
        mock: (uiState: HomeScreenUiState) -> Unit,
    ) {
        mock(vm.uiState.value)

        setHomeScreen {
            // Home screen
            val sectionRow = hasTestTag(UiConstants.TestTag.Item.HomeItemSectionRow)
                .waitUntilExactlyOneExists()

            sectionRow
                .onChildren()
                .assertCountEquals(ceil(AppConstants.Ui.HomeFavorite.ItemPerRow).toInt())

            repeat(data.size) { idx ->
                sectionRow
                    .performScrollToIndex(idx)

                hasText("test_$idx")
                    .waitUntilExactlyOneExists()
                    .assertIsDisplayed()
            }
        }
    }

    private fun setHomeScreen(block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                CompositionLocalProvider(
                    LocalItemStyle provides ItemStyleHolder.from(OSTestConfig.itemLayouts),
                ) {
                    OSTheme {
                        with(AppAndroidTestUtils.composeItemActionNavScopeTest()) {
                            HomeRoute(
                                viewModel = vm,
                                homeNavigation = HomeNavigation(
                                    navController = rememberNavController(),
                                    showBreadcrumb = {},
                                    breadcrumbNavigation = BreadcrumbNavigation(
                                        mainNavController = rememberNavController(),
                                        navigateBack = {},
                                        onCompositionNav = { null },
                                    ),
                                ),
                                showSnackBar = {},
                            )
                        }
                    }
                }
            }
            block()
        }
    }

    companion object {
        fun mockUiState(itemSize: Int = 0, block: HomeScreenUiState.() -> Unit = {}): HomeScreenUiState {
            return mockk<HomeScreenUiState> {
                val pagingData = if (itemSize <= 0) {
                    PagingData.from(
                        data = emptyList<PlainItemData>(),
                        sourceLoadStates = loadedPagingStates(),
                    )
                } else {
                    PagingData.from(
                        data = AppAndroidTestUtils.createPlainItemData(
                            size = itemSize,
                            itemNameProvider = { DefaultNameProvider("item $it") },
                            identifier = { LbcTextSpec.Raw("identifier_item_$it") },
                        ),
                        sourceLoadStates = loadedPagingStates(),
                    )
                }

                every { this@mockk.showFavoritesSeeAll } returns false
                every { this@mockk.items } returns flowOf(pagingData)
                every { this@mockk.favoriteItems } returns flowOf(persistentListOf())
                every { this@mockk.deletedItemCount } returns 0
                every { this@mockk.isAppBetaVersion } returns false
                every { this@mockk.shouldVerifyPassword } returns false
                every { this@mockk.hasDoneOnBoardingBubbles } returns false
                every { this@mockk.isBiometricEnabled } returns false
                every { this@mockk.isSafeReady } returns true
                every { this@mockk.initialItemCount } returns 0
                every { this@mockk.isLanguageGenerated } returns false

                block()
            }
        }
    }
}
