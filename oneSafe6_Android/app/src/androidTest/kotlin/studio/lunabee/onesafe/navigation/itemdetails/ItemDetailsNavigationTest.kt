package studio.lunabee.onesafe.navigation.itemdetails

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
abstract class ItemDetailsNavigationTest : OSMainActivityTest() {

    abstract override val hiltRule: HiltAndroidRule

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    protected lateinit var item: SafeItem
    protected val itemName: String = "test item"

    override val initialTestState: InitialTestState = InitialTestState.Home {
        item = createItemUseCase.test(name = itemName)
    }

    protected fun ComposeUiTest.navToItemDetails() {
        hasExcludeSearch(hasText(itemName))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
            .waitAndPrintRootToCacheDir(printRule, "_itemDetails_screen")
    }
}
