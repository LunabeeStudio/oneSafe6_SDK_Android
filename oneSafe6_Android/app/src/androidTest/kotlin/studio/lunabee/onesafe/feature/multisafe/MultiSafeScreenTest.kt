package studio.lunabee.onesafe.feature.multisafe

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class MultiSafeScreenTest : LbcComposeTest() {
    private val navigateBack: () -> Unit = spyk({})
    private val createNewSafe: () -> Unit = spyk({})

    @Test
    fun can_go_to_create_new_safe() {
        setMultiSafeScreen {
            hasText(getString(OSString.multiSafeOnBoarding_presentation_sectionTitle))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.multiSafeOnBoarding_presentation_message))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.multiSafeOnBoarding_presentation_sectionTitle))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.multiSafeOnBoarding_presentation_button))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
        }
        verify(exactly = 1) { createNewSafe.invoke() }
    }

    private fun setMultiSafeScreen(block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                OSTheme {
                    MultiSafePresentationRoute(
                        navigateBack = navigateBack,
                        createNewSafe = createNewSafe,
                    )
                }
            }
            block()
        }
    }
}
