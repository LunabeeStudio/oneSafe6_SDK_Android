package studio.lunabee.onesafe.navigation.itemform

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ItemFormScreenPasswordGeneratorTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home { }

    private fun navigateToForm(
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
            block()
        }
    }

    @Test
    fun display_password_generator_from_password_text_field() {
        navigateToForm {
            hasTestTag(UiConstants.TestTag.Item.GeneratePasswordAction)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.BottomSheet.PasswordGeneratorBottomSheet)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
            isRoot()
                .waitAndPrintWholeScreenToCacheDir(printRule)
        }
    }

    @Test
    fun save_generated_password_in_text_field_test() {
        navigateToForm {
            hasContentDescription(getString(OSString.safeItem_form_accessibility_generatePassword))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.BottomSheet.PasswordGeneratorBottomSheet)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
                .assertIsDisplayed()
            val password = hasTestTag(UiConstants.TestTag.Item.GeneratedPasswordText)
                .waitUntilExactlyOneExists()
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
            hasText(getString(OSString.common_confirm))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.BottomSheet.PasswordGeneratorBottomSheet)
                .waitUntilDoesNotExist()
            hasTestTag(testTag = UiConstants.TestTag.Item.VisibilityAction)
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(password.first().text)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
