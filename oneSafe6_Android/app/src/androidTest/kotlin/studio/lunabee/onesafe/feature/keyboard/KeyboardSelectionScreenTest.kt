package studio.lunabee.onesafe.feature.keyboard

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.keyboard.screen.KeyboardSelectionScreen

@OptIn(ExperimentalTestApi::class)
class KeyboardSelectionScreenTest : LbcComposeTest() {

    private val navigateBack: () -> Unit = spyk({})
    private val onClickOnEnableKeyboard: () -> Unit = spyk({})
    private val onClickOnSelectKeyboard: () -> Unit = spyk({})

    @Test
    fun test_enable_keyboard() {
        setScreen {
            hasText(getString(OSString.oneSafeK_onboarding_keyboardSelection_enableButton))
            onNodeWithText(getString(OSString.oneSafeK_onboarding_keyboardSelection_chooseButton)).assertDoesNotExist()
            onNodeWithText(getString(OSString.oneSafeK_onboarding_keyboardSelection_enableButton)).performClick()
            verify(exactly = 1) { onClickOnEnableKeyboard.invoke() }
            verify(exactly = 0) { onClickOnSelectKeyboard.invoke() }
        }
    }

    @Test
    fun test_select_keyboard() {
        setScreen(isKeyboardEnabled = true) {
            hasText(getString(OSString.oneSafeK_onboarding_keyboardSelection_enableButton))
            hasText(getString(OSString.oneSafeK_onboarding_keyboardSelection_chooseButton))
            onNodeWithText(getString(OSString.oneSafeK_onboarding_keyboardSelection_enableButton)).assertIsNotEnabled()
            onNodeWithText(getString(OSString.oneSafeK_onboarding_keyboardSelection_chooseButton)).performClick()
            verify(exactly = 0) { onClickOnEnableKeyboard.invoke() }
            verify(exactly = 1) { onClickOnSelectKeyboard.invoke() }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun setScreen(
        isKeyboardEnabled: Boolean = false,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                KeyboardSelectionScreen(
                    navigateBack = navigateBack,
                    onClickOnEnableKeyboard = onClickOnEnableKeyboard,
                    onClickOnSelectKeyboard = onClickOnSelectKeyboard,
                    isKeyboardEnabled = isKeyboardEnabled,
                )
            }
            block()
        }
    }
}
