package studio.lunabee.onesafe.navigation.share

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
class ShareNavigationTest : OSMainActivityTest() {

    private val lunabeeItemName: String = "Lunabee"
    private val googleItemName: String = "google"
    private lateinit var googleItemId: UUID

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @get:Rule(order = 0)
    override var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        val lunabeeItem = createItemUseCase.test(name = lunabeeItemName)
        createItemUseCase.test(name = "Mac", parentId = lunabeeItem.id)

        val googleItem = createItemUseCase.test(googleItemName)
        googleItemId = googleItem.id
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navigate_to_share_without_children_test() {
        invoke {
            hasExcludeSearch(hasText(googleItemName))
                .waitAndPrintRootToCacheDir(printRule, suffix = "_init")
                .performClick()

            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitAndPrintRootToCacheDir(printRule, "_share_action")
                .performScrollTo()
                .performClick()

            // Encrypting one item is very fast, cannot test that the "Encrypting" screen is displayed
            hasTestTag(UiConstants.TestTag.Screen.ShareFileScreen)
                .waitAndPrintRootToCacheDir(printRule, "_encryptShareScreen")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navigate_to_share_with_children_test() {
        invoke {
            hasExcludeSearch(hasText(lunabeeItemName))
                .waitAndPrintRootToCacheDir(printRule, suffix = "_init")
                .performClick()

            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitAndPrintRootToCacheDir(printRule, "_share_action")
                .performScrollTo()
                .performClick()

            isDialog()
                .waitAndPrintWholeScreenToCacheDir(printRule, suffix = "shareDialog")
                .assertIsDisplayed()

            onNodeWithText(getQuantityString(OSPlurals.share_itemWithChildrenDialog_includeChildrenButton, 1, 1))
                .performClick()

            // Encrypting one file is very fast, cannot test that the "Encrypting" screen is displayed
            hasTestTag(UiConstants.TestTag.Screen.ShareFileScreen)
                .waitAndPrintRootToCacheDir(printRule, "_encryptShareScreen")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun navigate_back_from_share_test() {
        invoke {
            hasExcludeSearch(hasText(googleItemName))
                .waitAndPrintRootToCacheDir(printRule)
                .performClick()

            hasText(getString(OSString.safeItemDetail_actionCard_share))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.ShareFileScreen)
                .waitAndPrintRootToCacheDir(printRule)

            Espresso.pressBack()

            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(googleItemId))
                .waitAndPrintRootToCacheDir(printRule)
        }
    }
}
