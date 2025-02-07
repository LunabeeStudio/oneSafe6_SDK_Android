package studio.lunabee.onesafe.feature.settings

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsScreen
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsUiState
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
class BubblesSettingsScreenTest : LbcComposeTest() {
    @Test
    fun has_never_done_oneSafeK_on_boarding_test() {
        setScreen {
            hasTestTag(UiConstants.TestTag.Item.OneSafeKStartOnBoardingCard)
            onNodeWithTag(UiConstants.TestTag.Item.OneSafeKStartOnBoardingCard).assertExists()
            onNodeWithTag(UiConstants.TestTag.Item.OneSafeKConfigurationCard).assertDoesNotExist()
        }
    }

    @Test
    fun has_done_oneSafeK_on_boarding_test() {
        setScreen(hasFinishOneSafeKOnBoarding = true) {
            onNodeWithTag(UiConstants.TestTag.Item.OneSafeKStartOnBoardingCard).assertDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.Item.OneSafeKConfigurationCard).assertExists()
        }
    }

    private fun setScreen(
        hasFinishOneSafeKOnBoarding: Boolean = false,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                BubblesSettingsScreen(
                    navigateBack = {},
                    uiState = BubblesSettingsUiState.default(hasFinishOneSafeKOnBoarding = hasFinishOneSafeKOnBoarding),
                    startOneSafeKOnBoarding = {},
                    setBubblesPreviewActivation = {},
                    onResendMessageClick = {},
                    onSelectAutoLockInactivityDelay = {},
                    onSelectAutoLockHiddenDelay = {},
                    featureFlagOneSafeK = true,
                    featureFlagFlorisBoard = true,
                )
            }
            block()
        }
    }
}
