package studio.lunabee.onesafe.feature.home

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavigation
import studio.lunabee.onesafe.feature.home.model.HomeConversationSectionData
import studio.lunabee.onesafe.feature.home.model.HomeInfoSectionData
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionsBottomSheet
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class HomeScreenVerifyPasswordTest : LbcComposeTest() {

    private val homeScreenUiState = HomeScreenTest.mockUiState()
    private val vm: HomeScreenViewModel = mockk {
        every { uiState } returns MutableStateFlow(homeScreenUiState)
        every { navigationAction } returns flowOf(null)
        every { itemActionDialogState } returns MutableStateFlow(null)
        every { itemActionSnackbarState } returns MutableStateFlow(null)
        every { areItemsBeingGenerated } returns MutableStateFlow(false)
        every { hasPreventionWarnings } returns MutableStateFlow(false)
        every { snackbarState } returns MutableStateFlow(null)
        every { osFeatureFlags.bubbles() } returns true
        every { shouldAskForTranslationSupport } returns MutableStateFlow(false)
        every { shouldAskForTranslationSupport(any()) } returns Unit
        every { homeInfoSectionData } returns MutableStateFlow(HomeInfoSectionData((emptyList())))
        every { homeConversationSection } returns MutableStateFlow(HomeConversationSectionData((emptyList())))
        every { itemDisplayOptionsBottomSheet } returns MutableStateFlow(
            ItemDisplayOptionsBottomSheet(
                onSelectItemOrder = {},
                selectedItemOrder = ItemOrder.Alphabetic,
                onSelectItemLayout = {},
                selectedItemLayout = ItemLayout.Grid,
            ),
        )
    }

    @Test
    fun home_need_to_verify_password_test(): TestResult = runTest {
        every { homeScreenUiState.shouldVerifyPassword } returns true

        setHomeScreen {
            onRoot().printToCacheDir(printRule)
            hasTestTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheet).waitUntilExactlyOneExists()
        }
    }

    @Test
    fun home_no_need_to_verify_password_test(): TestResult = runTest {
        every { homeScreenUiState.shouldVerifyPassword } returns false

        setHomeScreen {
            onRoot().printToCacheDir(printRule)
            onNodeWithTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheet).assertDoesNotExist()
        }
    }

    private fun setHomeScreen(block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
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
            block()
        }
    }
}
