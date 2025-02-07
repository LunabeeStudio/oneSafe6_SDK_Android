package studio.lunabee.onesafe.feature.onboarding

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.SplashScreenManager
import studio.lunabee.onesafe.feature.onboarding.presentation.AppPresentationRoute
import studio.lunabee.onesafe.feature.onboarding.presentation.AppPresentationViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class AppPresentationScreenTest : LbcComposeTest() {

    private val navigateToTermsScreen: () -> Unit = spyk({})

    @Test
    fun can_skip_presentation_test() {
        val nextMatcher = hasTestTag(UiConstants.TestTag.Item.AppPresentationNextButton)
        val skipMatcher = hasTestTag(UiConstants.TestTag.Item.AppPresentationSkipButton)

        setAppPresentationScreen {
            hasText(getString(OSString.appPresentation_security_action))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            skipMatcher.waitUntilDoesNotExist().assertDoesNotExist()

            nextMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()
            hasText(getString(OSString.appPresentation_personalization_action))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            skipMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()

            nextMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()
            hasText(getString(OSString.appPresentation_transparency_action))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            skipMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()

            nextMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()
            hasText(getString(OSString.appPresentation_setup_action))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            skipMatcher.waitUntilExactlyOneExists().assertIsDisplayed().performClick()
        }
        verify(exactly = 6) { navigateToTermsScreen.invoke() }
    }

    @Test
    fun can_see_all_presentation_steps_with_button_test() {
        val nextMatcher = hasTestTag(UiConstants.TestTag.Item.AppPresentationNextButton)

        setAppPresentationScreen {
            hasText(getString(OSString.appPresentation_security_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            nextMatcher
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            hasText(getString(OSString.appPresentation_personalization_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            nextMatcher
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            hasText(getString(OSString.appPresentation_transparency_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            nextMatcher
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            // Configure my safe - last step
            hasText(getString(OSString.appPresentation_setup_action))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            nextMatcher
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        }
        verify(exactly = 1) { navigateToTermsScreen.invoke() }
    }

    @Test
    fun can_see_all_presentation_steps_by_scrolling_test() {
        setAppPresentationScreen {
            hasText(getString(OSString.appPresentation_security_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.AppPresentationVerticalPager)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            hasText(getString(OSString.appPresentation_personalization_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.AppPresentationVerticalPager)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            hasText(getString(OSString.appPresentation_transparency_title))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.AppPresentationVerticalPager)
                .waitUntilExactlyOneExists()
                .performTouchInput { swipeUp() }
            hasText(getString(OSString.appPresentation_setup_action))
                .waitUntilExactlyOneExists()
                .performClick()
        }
        verify(exactly = 1) { navigateToTermsScreen.invoke() }
    }

    private fun setAppPresentationScreen(block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                OSTheme {
                    AppPresentationRoute(
                        navigateToNextStep = navigateToTermsScreen,
                        viewModel = AppPresentationViewModel(SplashScreenManager()),
                        // FIXME LaunchedEffect + while(true) for animation makes Espresso failed. It looks like infinite animations API
                        //  should works, but we can't change the target value on the fly to do our animation. So disable anim in test.
                        //  Explanation here -> https://developer.android.com/codelabs/jetpack-compose-testing#5
                        randomizePersonalizationBackground = false,
                    )
                }
            }
            block()
        }
    }
}
