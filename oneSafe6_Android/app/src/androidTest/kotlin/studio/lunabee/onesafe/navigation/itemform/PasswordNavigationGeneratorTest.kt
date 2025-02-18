package studio.lunabee.onesafe.navigation.itemform

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class PasswordNavigationGeneratorTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home { }

    private fun navigateToPasswordBottomSheet(
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
            hasText(getString(OSString.createItem_template_websiteElement))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            hasText(getString(OSString.fieldName_password))
                .waitUntilExactlyOneExists()
                .performTextInput("existingPassword")

            hasTestTag(UiConstants.TestTag.Item.GeneratePasswordAction)
                .waitUntilExactlyOneExists()
                .performClick()

            block()
        }
    }

    @Test
    fun override_dialog_password_test() {
        navigateToPasswordBottomSheet {
            hasText(getString(OSString.common_confirm))
                .waitUntilExactlyOneExists()
                .performClick()

            (hasText(getString(OSString.passwordGenerator_overrideDialog_message)) and hasAnyAncestor(isDialog()))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
